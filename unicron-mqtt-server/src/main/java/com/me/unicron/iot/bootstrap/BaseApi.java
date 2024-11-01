package com.me.unicron.iot.bootstrap;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 逻辑操作封装
 *
 * @author lianyadong
 * @create 2023-11-27 9:12
 **/
public interface BaseApi {


    default  <T> void  doIfElse(T t, Predicate<T> predicate, Consumer<T> consumer){
        if(t!=null){
            if(predicate.test(t)){
                consumer.accept(t);
            }
        }
    }


    default  <T> void  doIfElse(T t, Predicate<T> predicate, Consumer<T> consumer, Consumer<T> consumer2){
        if(t!=null){
            if(predicate.test(t)){
                consumer.accept(t);
            }
            else{
                consumer2.accept(t);
            }
        }
    }
    default  <T> boolean  doIf(T t, Predicate<T>... predicates){
        if(t!=null){
            for(Predicate<T> p:predicates){
                if(!p.test(t)){
                    return false;
                }
            }
            return true;
        }
        return  false;
    }

    default  <T> void   doIfAnd(T t, Consumer<T> consumer2, Predicate<T>... predicates){
        boolean flag =true;
        if(t!=null){
            for(Predicate<T> p:predicates){
                if(!p.test(t)){
                    flag= false;
                    break;
                }
            }
        }
        if(flag){
            consumer2.accept(t);
        }
    }

}
