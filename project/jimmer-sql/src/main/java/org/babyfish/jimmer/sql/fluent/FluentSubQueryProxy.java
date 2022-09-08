package org.babyfish.jimmer.sql.fluent;

import org.babyfish.jimmer.lang.OldChain;
import org.babyfish.jimmer.sql.ast.Expression;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.Selection;
import org.babyfish.jimmer.sql.ast.query.*;
import org.babyfish.jimmer.sql.ast.table.AssociationTableEx;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.ast.table.TableEx;
import org.babyfish.jimmer.sql.ast.tuple.*;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

class FluentSubQueryProxy implements MutableSubQuery {

    private final MutableSubQuery raw;

    private final Runnable onTerminate;

    FluentSubQueryProxy(MutableSubQuery raw, Runnable onTerminate) {
        this.raw = raw;
        this.onTerminate = onTerminate;
    }

    @Override
    public <T extends Table<?>, R> ConfigurableSubQuery<R> createSubQuery(
            Class<T> tableType,
            BiFunction<MutableSubQuery, T, ConfigurableSubQuery<R>> block
    ) {
        throw new UnsupportedOperationException(
                "FluentQuery does not support subQuery, please call `fluent.subQuery`"
        );
    }

    @Override
    public <T extends Table<?>> MutableSubQuery createWildSubQuery(
            Class<T> tableType,
            BiConsumer<MutableSubQuery, T> block
    ) {
        throw new UnsupportedOperationException(
                "FluentQuery does not support subQuery, please call `fluent.subQuery`"
        );
    }

    @Override
    public <SE, ST extends TableEx<SE>, TE, TT extends TableEx<TE>, R> ConfigurableSubQuery<R> createAssociationSubQuery(
            Class<ST> sourceTableType,
            Function<ST, TT> targetTableGetter,
            BiFunction<MutableSubQuery, AssociationTableEx<SE, ST, TE, TT>, ConfigurableSubQuery<R>> block
    ) {
        throw new UnsupportedOperationException(
                "FluentQuery does not support subQuery, please call `fluent.subQuery`"
        );
    }

    @Override
    public <SE, ST extends TableEx<SE>, TE, TT extends TableEx<TE>, R> MutableSubQuery createAssociationWildSubQuery(
            Class<ST> sourceTableType,
            Function<ST, TT> targetTableGetter, BiConsumer<MutableSubQuery, AssociationTableEx<SE, ST, TE, TT>> block
    ) {
        throw new UnsupportedOperationException(
                "FluentQuery does not support subQuery, please call `fluent.subQuery`"
        );
    }

    @Override
    @OldChain
    public MutableSubQuery where(Predicate... predicates) {
        raw.where(predicates);
        return this;
    }

    @Override
    @OldChain
    public MutableSubQuery orderBy(Expression<?> ... expressions) {
        raw.orderBy(expressions);
        return this;
    }

    @Override
    @OldChain
    public MutableSubQuery orderBy(Order... orders) {
        raw.orderBy(orders);
        return this;
    }

    @Override
    @OldChain
    public MutableSubQuery groupBy(Expression<?>... expressions) {
        raw.groupBy(expressions);
        return this;
    }

    @Override
    @OldChain
    public MutableSubQuery having(Predicate... predicates) {
        raw.having(predicates);
        return this;
    }

    @Override
    public Predicate exists() {
        Predicate result = raw.exists();
        onTerminate.run();
        return result;
    }

    @Override
    public Predicate notExists() {
        Predicate result = raw.notExists();
        onTerminate.run();
        return result;
    }

    @Override
    public <R> ConfigurableSubQuery<R> select(Selection<R> selection) {
        ConfigurableSubQuery<R> result = raw.select(selection);
        onTerminate.run();
        return result;
    }

    @Override
    public <T1, T2> ConfigurableSubQuery<Tuple2<T1, T2>> select(
            Selection<T1> selection1,
            Selection<T2> selection2
    ) {
        ConfigurableSubQuery<Tuple2<T1, T2>> result = raw.select(selection1, selection2);
        onTerminate.run();
        return result;
    }

    @Override
    public <T1, T2, T3> ConfigurableSubQuery<Tuple3<T1, T2, T3>> select(
            Selection<T1> selection1,
            Selection<T2> selection2,
            Selection<T3> selection3
    ) {
        ConfigurableSubQuery<Tuple3<T1, T2, T3>> result =  raw.select(selection1, selection2, selection3);
        onTerminate.run();
        return result;
    }

    @Override
    public <T1, T2, T3, T4> ConfigurableSubQuery<Tuple4<T1, T2, T3, T4>> select(
            Selection<T1> selection1,
            Selection<T2> selection2,
            Selection<T3> selection3,
            Selection<T4> selection4
    ) {
        ConfigurableSubQuery<Tuple4<T1, T2, T3, T4>> result =
                raw.select(selection1, selection2, selection3, selection4);
        onTerminate.run();
        return result;
    }

    @Override
    public <T1, T2, T3, T4, T5> ConfigurableSubQuery<Tuple5<T1, T2, T3, T4, T5>> select(
            Selection<T1> selection1,
            Selection<T2> selection2,
            Selection<T3> selection3,
            Selection<T4> selection4,
            Selection<T5> selection5
    ) {
        ConfigurableSubQuery<Tuple5<T1, T2, T3, T4, T5>> result =
                raw.select(selection1, selection2, selection3, selection4, selection5);
        onTerminate.run();
        return result;
    }

    @Override
    public <T1, T2, T3, T4, T5, T6> ConfigurableSubQuery<Tuple6<T1, T2, T3, T4, T5, T6>> select(
            Selection<T1> selection1,
            Selection<T2> selection2,
            Selection<T3> selection3,
            Selection<T4> selection4,
            Selection<T5> selection5,
            Selection<T6> selection6
    ) {
        ConfigurableSubQuery<Tuple6<T1, T2, T3, T4, T5, T6>> result =
                raw.select(selection1, selection2, selection3, selection4, selection5, selection6);
        onTerminate.run();
        return result;
    }

    @Override
    public <T1, T2, T3, T4, T5, T6, T7> ConfigurableSubQuery<Tuple7<T1, T2, T3, T4, T5, T6, T7>> select(
            Selection<T1> selection1,
            Selection<T2> selection2,
            Selection<T3> selection3,
            Selection<T4> selection4,
            Selection<T5> selection5,
            Selection<T6> selection6,
            Selection<T7> selection7
    ) {
        ConfigurableSubQuery<Tuple7<T1, T2, T3, T4, T5, T6, T7>> result =
                raw.select(selection1, selection2, selection3, selection4, selection5, selection6, selection7);
        onTerminate.run();
        return result;
    }

    @Override
    public <T1, T2, T3, T4, T5, T6, T7, T8> ConfigurableSubQuery<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> select(
            Selection<T1> selection1,
            Selection<T2> selection2,
            Selection<T3> selection3,
            Selection<T4> selection4,
            Selection<T5> selection5,
            Selection<T6> selection6,
            Selection<T7> selection7,
            Selection<T8> selection8
    ) {
        ConfigurableSubQuery<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> result = raw.select(
                selection1,
                selection2,
                selection3,
                selection4,
                selection5,
                selection6,
                selection7,
                selection8
        );
        onTerminate.run();
        return result;
    }

    @Override
    public <T1, T2, T3, T4, T5, T6, T7, T8, T9> ConfigurableSubQuery<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> select(
            Selection<T1> selection1,
            Selection<T2> selection2,
            Selection<T3> selection3,
            Selection<T4> selection4,
            Selection<T5> selection5,
            Selection<T6> selection6,
            Selection<T7> selection7,
            Selection<T8> selection8,
            Selection<T9> selection9
    ) {
        ConfigurableSubQuery<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> result =
                raw.select(
                        selection1,
                        selection2,
                        selection3,
                        selection4,
                        selection5,
                        selection6,
                        selection7,
                        selection8,
                        selection9
                );
        onTerminate.run();
        return result;
    }
}
