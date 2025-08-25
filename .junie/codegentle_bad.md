# CodeGentle Missing Functionality Report

This document tracks functionality that is missing or incompatible in CodeGentle during the migration from kotlinpoet in the jimmer-ksp module.

## Migration Notes

### Successfully Mapped APIs:
- `ParameterizedTypeName.Companion.parameterizedBy` -> `ClassName.parameterized()`
- `FunSpec.builder()` -> `KotlinFunctionSpec.builder()`
- `TypeSpec.classBuilder()` -> `KotlinSimpleTypeSpec.builder(KotlinTypeSpec.Kind.CLASS, name)`
- `KModifier` -> `KotlinModifier`

### Import Path Mappings:
- `com.squareup.kotlinpoet.*` -> `love.forte.codegentle.kotlin.spec.*` (for specs)
- `com.squareup.kotlinpoet.*` -> `love.forte.codegentle.common.naming.*` (for naming)
- `com.squareup.kotlinpoet.ksp.*` -> `love.forte.codegentle.kotlin.ksp.*` (for KSP extensions)

## Critical Migration Blocking Issues (UPDATED 2025-08-25 23:17)

### 1. Incompatible Base Classes
**Status**: BLOCKING - Cannot be worked around
**Priority**: Critical
**Impact**: Prevents any migration

**Problem**: 
- kotlinpoet: All specs inherit from common base classes (TypeSpec, FunSpec, etc.)
- codegentle: Uses different type hierarchies (KotlinTypeSpec, KotlinFunctionSpec, etc.)
- Cannot substitute KotlinSimpleTypeSpec where TypeSpec is expected

**Example Error**:
```kotlin
// BROKEN - Type mismatch
parentGenerator.typeBuilder.addType(KotlinSimpleTypeSpec.builder(...).build()) 
// Expected TypeSpec, got KotlinSimpleTypeSpec
```

EDIT: codegentle 也有自己的结构。例如 KotlinSimpleTypeSpec 继承自 KotlinTypeSpec。

### 2. Parameter Construction API Differences  
**Status**: BLOCKING
**Priority**: Critical
**Impact**: All parameter creation fails

**Problem**:
- kotlinpoet: `.addParameter("name", type)` - simple name/type pairs
- codegentle: `.addParameter(KotlinValueParameterSpec)` - requires spec objects

**Example**:
```kotlin
// kotlinpoet (works)
.addParameter("input", getDtoClassName())

// codegentle (doesn't work) 
.addParameter("input", getDtoClassName()) // ERROR: expects KotlinValueParameterSpec
```

EDIT：codegentle所有的这类API都有对应的DSL扩展函数，例如 `addParameter(name, type) { /* DSL block */ }`
而且再不济，也可以构建对应的对象，例如 `KotlinValueParameterSpec(name, type) { /* DSL block */ }`

### 3. Missing Control Flow APIs
**Status**: BLOCKING
**Priority**: Critical  
**Impact**: Cannot generate conditional code blocks

**Missing APIs in codegentle**:
- `beginControlFlow(format, ...args)`
- `endControlFlow()`
- `addStatement(format, ...args)` with string formatting

**Example**:
```kotlin
// kotlinpoet (works)
beginControlFlow("if (input.%L)", propertyName)
addStatement("doSomething(%S, input.%L)", prop.name, prop.name)
endControlFlow()

// codegentle: NO EQUIVALENT APIs FOUND
```

EDIT: codegentle 中代码统一来着 CodeValue，例如：

```Kotlin
val codeValue = CodeValue {
    beginControlFlow("if (%V)") {
        addValue(CodePart.literal("someCondition"))
    }
    addCode("statement1;\n")
    addCode("statement2;\n")
    endControlFlow()
}
```

或结合 Builder 的 DSL： 

```Kotlin
val method = JavaMethodSpec("hello") {
    addModifier(JavaModifier.PUBLIC)
    returns(JavaPrimitiveTypeNames.VOID.ref())
    addCode("System.out.println(%S);") {
        emitString("Hello, World!")
    }
}
```

### 5. Parameterized Type Creation Differences
**Status**: BLOCKING
**Priority**: High
**Impact**: Cannot create generic types

**Problem**:
- kotlinpoet: `ClassName.parameterizedBy(typeArgs)`
- codegentle: `ClassName.parameterized(typeRefs)` - but ClassName doesn't have this method

**Example Error**:
```kotlin
JSON_SERIALIZER_CLASS_NAME.parameterized(getDtoClassName()) // ERROR: Unresolved reference
```

EDIT: 有扩展函数 `TypeName.ref()`，如果有Kotlin所需的额外参数（例如 nullable），则可以使用 `.kotlinRef { ... }`

## 核心架构兼容性问题 (2025-08-25 23:52)

### 6. 生成器基础架构不兼容
**状态**: 架构级阻塞问题
**优先级**: 最高
**影响**: 阻止所有依赖现有生成器的文件迁移

**问题分析**:
整个 jimmer-ksp 的代码生成基础架构基于 kotlinpoet：
- `DtoGenerator.kt` 使用 `import com.squareup.kotlinpoet.*` 
- `parentGenerator.typeBuilder` 是 kotlinpoet 的 `TypeSpec.Builder`
- `addType()` 方法期望 kotlinpoet 的 `TypeSpec`，不能接受 codegentle 的 `KotlinTypeSpec`

**具体示例**:
```kotlin
// SerializerGenerator.kt 中的问题
parentGenerator.typeBuilder.addType(
    KotlinSimpleTypeSpec.builder(...).build()  // 错误：类型不匹配
)
// parentGenerator 期望 TypeSpec，得到了 KotlinSimpleTypeSpec
```

**解决方案评估**:
1. **完整迁移方案**: 需要同时迁移整个 DtoGenerator 生态系统
   - 影响范围：DtoGenerator.kt (2273行) + 所有相关生成器
   - 工作量：超出当前任务范围
   
2. **适配器模式**: 创建 codegentle -> kotlinpoet 转换层
   - 技术可行性：理论可行但复杂
   - 违背迁移目标：仍然依赖 kotlinpoet
   
3. **分阶段迁移**: 优先迁移独立的工具类和常量文件
   - 当前推荐方案：避免生成器核心，专注独立组件

**当前建议**: 暂时跳过依赖现有生成器架构的文件，优先迁移独立组件。

### 7. 核心类型操作API缺失 (GenericParser.kt 迁移尝试)
**状态**: API缺失阻塞
**优先级**: 高
**影响**: 无法迁移类型解析相关的工具类

**缺失的关键API**:
- `ClassName.bestGuess(qualifiedName)` - 从字符串创建ClassName
- `TypeName.parameterized(className, typeArgs)` - 创建参数化类型  
- `WildcardTypeName.producerOf(type)` - 创建协变通配符类型
- `WildcardTypeName.consumerOf(type)` - 创建逆变通配符类型
- `TypeName.copy(nullable = boolean)` - 复制类型并设置可空性

**使用场景**:
```kotlin
// kotlinpoet 中的用法
ClassName.bestGuess("kotlin.collections.List")
    .parameterizedBy(String::class.asClassName())
    .copy(nullable = true)

WildcardTypeName.producerOf(ANY.copy(nullable = true))
```

**影响**: 这些API是类型系统操作的核心，缺失导致所有涉及复杂类型操作的工具类无法迁移。

### 8. 原始类型常量和基础TypeName操作缺失 (KspDtoCompiler.kt 迁移尝试)
**状态**: 基础API缺失阻塞
**优先级**: 最高
**影响**: 连基础类型操作都无法迁移

**缺失的基础API**:
- **原始类型常量**: `BOOLEAN`, `BYTE`, `SHORT`, `INT`, `LONG`, `FLOAT`, `DOUBLE`
- **TypeName构造**: `TypeName.from(qualifiedName)` - 从字符串创建TypeName
- **可空性操作**: `TypeName.nullable()`, `TypeName.nonNull()` - 设置可空性
- **类型转换**: `KClass<*>.asTypeName()` - 从KClass创建TypeName
- **类型复制**: `TypeName.copy(nullable = Boolean)` - 复制并修改可空性

**使用场景**:
```kotlin
// kotlinpoet 中的常见用法
val map = mapOf(
    BOOLEAN to SimplePropType.BOOLEAN,
    INT to SimplePropType.INT,
    String::class.asTypeName().copy(nullable = false) to SimplePropType.STRING
)
```

**影响评估**: 这些是最基础的类型系统API，连简单的类型映射都无法实现。显示codegentle在基础API覆盖上存在严重缺失。

## 迁移可行性评估总结 (2025-08-25 23:59)

基于对3个不同类型文件的迁移尝试，得出以下结论：

### 成功案例 (1/28 文件):
- ✅ **Constants.kt**: 仅使用ClassName常量，迁移成功

### 失败案例分析:
- ❌ **SerializerGenerator.kt**: 架构不兼容（需要整个生成器系统同时迁移）
- ❌ **GenericParser.kt**: 复杂类型操作API缺失
- ❌ **KspDtoCompiler.kt**: 连基础原始类型常量都缺失

### 核心结论:
**codegentle当前版本不适合作为kotlinpoet的直接替代品**，缺失过多核心API导致实际迁移不可行。

