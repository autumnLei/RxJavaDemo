package com.example.administrator.rxjavademo;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "main_";
    private TextView textView;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==0){
                Toast.makeText(MainActivity.this, "收到message一枚", Toast.LENGTH_SHORT).show();
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text_view);
//        test();
        //test2();
//        test3();
//        test4();
          test5();
}

    public void handerTest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Message message = Message.obtain();
                message.arg1=1;
                handler.sendMessage(message);
            }
        }).start();
    }

    public void test(){
        //创建被观察者
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                //调用观察者的回调
                emitter.onNext("我是");
                emitter.onNext("RxJava");
                emitter.onNext("简单示例");
                emitter.onError(new Throwable("出错了"));
                emitter.onComplete();
            }
        });

        //创建观察者
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe: ");
            }

            @Override
            public void onNext(String s) {
                Log.d(TAG, "onNext: "+s);
            }   

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError: "+e.getMessage());
            }

            //onCompleted和onError不会同时调用，只会调用其中之一
            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");
            }

        };

        //注册，将观察者和被观察者关联，将会触发OnSubscribe.call方法
        observable.subscribe(observer);
    }

    public void test2(){
        //创建被观察者
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d(TAG, "Observable thread is :" + Thread.currentThread().getName());
                emitter.onNext(getResponse());
            }
        });

        //创建观察者
        Consumer<String> consumer = new Consumer<String>() {
            @Override
            public void accept(String mResponse) throws Exception {
                Log.d(TAG, "Observer thread is :" + Thread.currentThread().getName());
                textView.setText(mResponse);
            }
        };
        //subscribeOn() 指定的是发送事件的线程, observeOn() 指定的是接收事件的线程.
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    public void test3(){
        //创建被观察者
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
//                Thread.sleep(5000);
                Log.d(TAG, "Observable thread is :" + Thread.currentThread().getName());
                emitter.onNext(getResponse());
//                emitter.onNext("hi man!");
            }
            //通过map操作符对数据进行中间处理
        }).map(new Function<String, Integer>() {
            @Override
            public Integer apply(@NonNull String response) throws Exception {
                return response.length();
            }
        });
        //创建观察者
        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d(TAG, "Observer thread is :" + Thread.currentThread().getName());
                Log.d(TAG, "accept: words:"+integer);
                textView.setText("字数："+integer);
            }
        };
        //绑定，指定线程
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
  }

    public void test4(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d(TAG, "Observable thread is :" + Thread.currentThread().getName());
                emitter.onNext(getResponse());
            }
        }).map(new Function<String, Integer>() {
            @Override
            public Integer apply(@NonNull String response) throws Exception {
                return response.length();
            }
        }).subscribeOn(Schedulers.newThread())
//        AndroidSchedulers.mainThread() 同下
          .observeOn(AndroidSchedulers.from(getMainLooper()))
          .subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                Log.d(TAG, "Observer thread is :" + Thread.currentThread().getName());
                textView.setText("字数：" + integer);
            }
        });
    }

    public void test5(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) {
            e.onNext("hi");
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String o) {
                Log.d(TAG, "onNext: "+o);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }

        });
    }
    /**
     * 使用okhttp访问网上提供的接口     *
     * @return
     */
    private String getResponse() {
        try {
//            String url = "http://guolin.tech/api/china";
            String url = "https://www.baidu.com/";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().tag(this)
                    .url(url)
                    .build();
            Response response;
            response = client.newCall(request).execute();

            return response.body().string();
        } catch (IOException e) {
            return "error";
        }

    }




}
