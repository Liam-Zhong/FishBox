package com.example.fishcodex

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

/** 图片首次加载后缓存到本机磁盘，之后不再重复请求站点 */
class FishApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
}
