package ble.lss.com.bleutil.common;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;

/**
 * Created by admin on 2018/4/11.
 */

public class RxUtil {

    public static class EmitterProxy<T> implements ObservableEmitter<T>{
        private  ObservableEmitter<T> target;
        public void setTarget( ObservableEmitter<T> target){
            this.target = target;
        }
        @Override
        public void onError(@NonNull Throwable error) {
            if(target==null){
                return;
            }
            target.onError(error);
        }

        @Override
        public void onComplete() {
            if(target==null){
                return;
            }
            target.onComplete();
        }

        @Override
        public void onNext(@NonNull T value) {
            if(target==null){
                return;
            }
            target.onNext(value);
        }

        @Override
        public void setDisposable(@Nullable Disposable d) {
            if(target==null){
                return;
            }
            target.setDisposable(d);
        }

        @Override
        public void setCancellable(@Nullable Cancellable c) {
            if(target==null){
                return;
            }
            target.setCancellable(c);
        }
        @Override
        public boolean isDisposed() {
            if(target==null){
                return true;
            }
            return target.isDisposed();
        }
        @Override
        public ObservableEmitter<T> serialize() {
            if(target==null){
                return null;
            }
            return target.serialize();
        }
        @Override
        public boolean tryOnError(@NonNull Throwable t) {
            if(target==null){
                return false;
            }
            return target.tryOnError(t);
        }
    }

    public static <T> Observable<T> createSingleErr(final Throwable err){
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> e) throws Exception {
                e.onError(err);
            }
        });
    }
    public static <T> Observable createSingleNext(final T data){
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> e) throws Exception {
                e.onNext(data);
                e.onComplete();
            }
        });
    }
}
