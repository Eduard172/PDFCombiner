package me.edward.pdfcombiner;

@SuppressWarnings("unused")
public class Pair <K, V> {

    private K k;
    private V v;

    public Pair(K k, V v){
        this.setBoth(k, v);
    }

    public void setKey(K k){
        this.k = k;
    }

    public void setValue(V v){
        this.v = v;
    }

    public void setBoth(K k, V v){
        this.setKey(k);
        this.setValue(v);
    }

    public K getKey(){
        return this.k;
    }

    public V getValue(){
        return this.v;
    }

    public static<K, V> Pair<K, V> of(K k, V v){
        return new Pair<>(k, v);
    }

}
