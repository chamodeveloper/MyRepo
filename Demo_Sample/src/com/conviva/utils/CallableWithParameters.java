// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.utils;

/**
 * This class provides ways of implementing anonymous functions which can be
 * passed as parameters Java does not directly support passing functions as
 * parameters. To pass a function as a function parameter, we can create an
 * object that implements one of the following interfaces
 */
public class CallableWithParameters {

    /**
     * Interface with no parameter.
     */
    public static interface With0 {
        void exec();
    }

    /**
     * Interface with single parameter.
     * @param <T> parameter one
     */
    public static interface With1<T> {
        void exec(T param);
    }

    /**
     * Interface with two parameters.
     *
     * @param <T1> parameter one.
     * @param <T2> parameter two.
     */
    public static interface With2<T1, T2> {
        void exec(T1 param1, T2 param2);
    }

    /**
     *  Interface with three parameters.
     *
     * @param <T1> parameter one.
     * @param <T2> parameter two.
     * @param <T3> parameter three.
     */
    public static interface With3<T1, T2, T3> {
        void exec(T1 param1, T2 param2, T3 param3);
    }

    /** Interface with five parameters.
     *
     * @param <T1> parameter one.
     * @param <T2> parameter two.
     * @param <T3> parameter three.
     * @param <T4> parameter four.
     * @param <T5> parameter five.
     */
    public static interface With5<T1, T2, T3, T4, T5> {
        void exec(T1 parm1, T2 param2, T3 param3, T4 param4, T5 param5);
    }

    /**
     * Interface with one parameter and one return value.
     *
     * @param <P1> parameter one.
     * @param <R2> return value.
     */
    public static interface With1Return1<P1, R2>{
        R2 call(P1 parm1);
    }
}
