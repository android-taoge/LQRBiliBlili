package com.lqr.biliblili;

import android.app.Application;
import android.support.v4.app.FragmentManager;
import android.content.Context;

import com.jess.arms.base.delegate.AppLifecycles;
import com.jess.arms.di.module.GlobalConfigModule;
import com.jess.arms.http.GlobalHttpHandler;
import com.jess.arms.integration.ConfigModule;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class GlobalConfiguration implements ConfigModule {

    @Override
    public void applyOptions(Context context, GlobalConfigModule.Builder builder) {
        //使用builder可以为框架配置一些配置信息
        builder
//                .baseurl(Api.APP_DOMAIN)
                // 当数据无法加载时，使用过期数据
                .rxCacheConfiguration((context1, builder1) -> builder1.useExpiredDataIfLoaderNotAvailable(true))
                .globalHttpHandler(new GlobalHttpHandler() {
                    @Override
                    public Response onHttpResultResponse(String httpResult, Interceptor.Chain chain, Response response) {
                        // 统一处理http响应。eg:状态码不是200时，根据状态码做相应的处理。
                        return response;
                    }

                    @Override
                    public Request onHttpRequestBefore(Interceptor.Chain chain, Request request) {
                        // 统一处理http请求。eg:给request统一添加token或者header以及参数加密等操作
                        return request;
                    }
                })
                .responseErrorListener((context12, t) -> {
                    /* 用来提供处理所有错误的监听
                       rxjava必要要使用ErrorHandleSubscriber(默认实现Subscriber的onError方法),此监听才生效 */

                });
//                .cacheFile(New File("cache"));
    }

    @Override
    public void injectAppLifecycle(Context context, List<AppLifecycles> lifecycles) {
        //向Application的生命周期中注入一些自定义逻辑
        lifecycles.add(new AppLifecycles() {
            // LeakCanary观察器
            private RefWatcher mRefWatcher;

            @Override
            public void onCreate(Application application) {
                if (BuildConfig.LOG_DEBUG) {
                    //Timber日志打印
                    Timber.plant(new Timber.DebugTree());
                }
                //leakCanary内存泄露检查
                mRefWatcher = BuildConfig.USE_CANARY ? LeakCanary.install(application) : RefWatcher.DISABLED;
            }

            @Override
            public void attachBaseContext(Context base) {

            }

            @Override
            public void onTerminate(Application application) {
                mRefWatcher = null;
            }
        });
    }

    @Override
    public void injectActivityLifecycle(Context context, List<Application.ActivityLifecycleCallbacks> lifecycles) {
        //向Activity的生命周期中注入一些自定义逻辑
    }


    @Override
    public void injectFragmentLifecycle(Context context, List<FragmentManager.FragmentLifecycleCallbacks> lifecycles) {
        //向Fragment的生命周期中注入一些自定义逻辑
    }
}