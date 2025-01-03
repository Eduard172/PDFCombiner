package me.edward.pdfcombiner;

/**
 * This object was created primarily for bypassing the lambda expression restrictions
 */
public class LambdaObj <T> {

    private T t;

    public LambdaObj(T t){
        this.set(t);
    }

    public LambdaObj(){
        this.set(null);
    }

    public void set(T t){
        this.t = t;
    }

    public T get(){
        return this.t;
    }

    /**
     * Returns a new instance of this object with a given value.
     */
    public static <T> LambdaObj<T> of(T t){
        return new LambdaObj<>(t);
    }

    /**
     * Returns a new instance of this object which has a null initial value
     */
    public static <T> LambdaObj<T> of(){
        return new LambdaObj<>();
    }

}
