package com.jinhaihan.hydrogenlivewallpaper

import android.content.Context
import android.graphics.*
import android.util.Log
import com.tencent.mmkv.MMKV
import com.wildma.pictureselector.PictureBean
import org.greenrobot.eventbus.EventBus


class WallpaperUtils {
    companion object{
        var created = false
        var TAG = "WallpaperUtils"
        var lastPaint : Paint? = null
        var lastBitmap : Bitmap ? = null

        var finalFirstBitmap : Bitmap?=null
        var finalSecondBitmap : Bitmap?=null

        var isFirstNeedProcess : Boolean = true
        var isSecondPageGradient : Boolean = true

        var isUserSavedColor : Boolean = false
        var userSecondPageColor : Int? = null

        var isSecondPageBitmapGradient = false

        //MainActivity和Service都需要读取设置，为防止重复读取，设立此项
        var isSettingDone = false

        fun makeTwoBitmap(bitmap: Bitmap,darkColor: Int, color: Int){
            finalFirstBitmap = makeFirstBitmap(bitmap,darkColor,color)
            finalSecondBitmap = makeSecondBitmap(bitmap,darkColor,color)
        }

        fun makeFirstBitmap(bitmap: Bitmap,darkColor: Int, color: Int):Bitmap{
            return if(!isFirstNeedProcess) bitmap
            else getGradientBitMap(bitmap,darkColor, color)
        }

        fun makeSecondBitmap(bitmap: Bitmap,darkColor: Int, color: Int):Bitmap{
            var canvas = Canvas(bitmap)
            if(isSecondPageGradient){
                //渲染渐变色
                var bit = getGradient(bitmap,darkColor,color)
                canvas.drawBitmap(bit,0f,0f,null)

                if(isSecondPageBitmapGradient){
                    //渲染原图渐变
                    canvas.drawBitmap(getGradientBitMap(bitmap,darkColor,color),0f,0f,null)
                }
            }
            else{
                //渲染用户色
                val paint = Paint()

                if(isUserSavedColor){
                    paint.color = userSecondPageColor!!
                }
                else {
                    paint.color = color
                }
                val rectF = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
                canvas.drawRect(rectF, lastPaint!!)
                if(isSecondPageBitmapGradient){
                    //渲染原图渐变
                    canvas.drawBitmap(getGradientBitMap(bitmap,darkColor,color),0f,0f,null)
                }
            }
            return bitmap
        }

        /**
         * 创建自上而下的渐变Bitmap
         */
        fun getGradientBitMap(bgBitmap: Bitmap,darkColor: Int, color: Int):Bitmap{
            val bitmap = Bitmap.createBitmap(bgBitmap.width,bgBitmap.height,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(getGradient(bgBitmap,darkColor,color),0f,0f,null)
            canvas.drawBitmap(getImageToChange(bgBitmap)!!,0f,0f,null)
            return bitmap
        }

        /**
         * 创建对应颜色的渐变效果
         */
        fun getGradient(bgBitmap: Bitmap,darkColor: Int, color: Int):Bitmap{
            val bitmap = Bitmap.createBitmap(bgBitmap.width,bgBitmap.height,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val bgColors = IntArray(2)
            bgColors[0] = darkColor
            bgColors[1] = color
            val paint = Paint()
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            val gradient = LinearGradient(
                0f,
                0f,
                0f,
                canvas.height.toFloat(),
                bgColors[0],
                bgColors[1],
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            paint.style = Paint.Style.FILL
            val rectF = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
            // mCanvas.drawRoundRect(rectF,16,16,mPaint); 这个用来绘制圆角的哈
            canvas.drawRect(rectF, paint)
            return bitmap
        }

        //创建线性渐变背景色
        fun createLinearGradientBitmap(mCanvas: Canvas,bgBitmap: Bitmap,darkColor: Int, color: Int) {
            //是否需要绘制渐变色
            if(isSecondPageGradient){
                val bgColors = IntArray(2)
                bgColors[0] = darkColor
                bgColors[1] = color
                lastPaint = Paint()
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                val gradient = LinearGradient(
                    0f,
                    0f,
                    0f,
                    mCanvas.height.toFloat(),
                    bgColors[0],
                    bgColors[1],
                    Shader.TileMode.CLAMP
                )
                lastPaint!!.shader = gradient
                lastPaint!!.style = Paint.Style.FILL
            }
            else{
                lastPaint = Paint()
                if(isUserSavedColor)
                    lastPaint!!.color = userSecondPageColor!!
                else {
                    lastPaint!!.color = color
                }
            }

            val rectF = RectF(0f, 0f, mCanvas.width.toFloat(), mCanvas.height.toFloat())
            // mCanvas.drawRoundRect(rectF,16,16,mPaint); 这个用来绘制圆角的哈
            mCanvas.drawRect(rectF, lastPaint!!)
            if(isSecondPageBitmapGradient){
                mCanvas.drawBitmap(getImageToChange(bgBitmap)!!,0f,0f,lastPaint!!)
            }


            if(isFirstNeedProcess){
                lastBitmap = getImageToChange(bgBitmap)!!
            }
            val xScale: Float = (mCanvas.width.toFloat()  ) / lastBitmap!!.width
            lastBitmap = Bitmap.createScaledBitmap(lastBitmap!!,(xScale*(lastBitmap!!.width)).toInt(),(xScale*lastBitmap!!.height).toInt(),true)
            mCanvas.drawBitmap(lastBitmap!!,0f,0f,lastPaint!!)
            EventBus.getDefault().post(EventMessage())
        }

        fun createPreviewBitmaps(bgBitmap: Bitmap, darkColor: Int, color: Int){
            finalFirstBitmap = Bitmap.createBitmap(bgBitmap.width,bgBitmap.height,Bitmap.Config.ARGB_8888)
            var mCanvas = Canvas(finalFirstBitmap!!)

            if(isSecondPageGradient){
                val bgColors = IntArray(2)
                bgColors[0] = darkColor
                bgColors[1] = color
                lastPaint = Paint()
                lastPaint!!.isDither = true
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                val gradient = LinearGradient(
                    0f,
                    0f,
                    0f,
                    bgBitmap.height.toFloat(),
                    bgColors[0],
                    bgColors[1],
                    Shader.TileMode.CLAMP
                )

                lastPaint!!.shader = gradient
            }
            else{
                lastPaint = Paint()
                lastPaint!!.isDither = true
                if(isUserSavedColor){
                    Log.i(TAG,"userSecondPageColor：null" + color.toString())
                    lastPaint!!.color = userSecondPageColor!!
                }
                else {
                    lastPaint!!.color = color
                    Log.i(TAG, "userSecondPageColor:$color")
                }
            }

            val rectF = RectF(0f, 0f, bgBitmap.width.toFloat(), bgBitmap.height.toFloat())
            mCanvas.drawRect(rectF, lastPaint!!)
            if(isSecondPageBitmapGradient){
                mCanvas.drawBitmap(getImageToChange(bgBitmap)!!,0f,0f,lastPaint!!)
            }

            finalSecondBitmap = finalFirstBitmap!!.copy(Bitmap.Config.ARGB_8888,true) //目前first是在canvas中处理过的渐变
            lastBitmap = if(isFirstNeedProcess){
                getImageToChange(bgBitmap)!! //检测是否需要渐变处理

            } else{
                bgBitmap
            }
            mCanvas.drawBitmap(lastBitmap!!,0f,0f,lastPaint!!) //将lastBitmap绘制到first
        }

        /**
         * 图渐变算法
         */
        fun getImageToChange(mBitmap: Bitmap): Bitmap? {
            //Log.d(TAG, "with=" + mBitmap.width + "--height=" + mBitmap.height)
            val createBitmap = Bitmap.createBitmap(
                mBitmap.width,
                mBitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val mWidth = mBitmap.width
            val mHeight = mBitmap.height
            Log.i(TAG,"位图高度："+ mBitmap.height)
            for (i in 0 until mHeight) {
                for (j in 0 until mWidth) {
                    var color = mBitmap.getPixel(j, i)
                    val g = Color.green(color)
                    val r = Color.red(color)
                    val b = Color.blue(color)
                    var a = Color.alpha(color)
                    val index = i * 1.0f / mHeight
                    if (index > 0.25f) {
                        //val temp = i - mHeight / 4.0f
                        //旧算法
                        //a = 255 - (temp / 700 * 255).toInt()
                        a = 255 - (((i * 1.0f / mHeight) - 0.3f) * 3.3 *255).toInt()
                        //Log.e("Testssss","255-((("+i.toString() + "/" + mHeight.toString() + ") - 0,25f ) * 2 * 255 = " + a.toString())
                        if(a<0) a = 0
                        else if(a>255) a = 255
                    }
                    color = Color.argb(a, r, g, b)
                    createBitmap.setPixel(j, i, color)
                }
            }
            return createBitmap
        }

        fun getSavedImage(context: Context):Bitmap?{
            var kv = MMKV.defaultMMKV()
            var bean = kv.decodeParcelable("ImagePath",PictureBean::class.java)
            return if(bean != null){
                var op = BitmapFactory.Options()
                op.inPreferredConfig = Bitmap.Config.ARGB_8888
                BitmapFactory.decodeFile(bean.path, op)?.copy(Bitmap.Config.ARGB_8888,true)
            } else{
                null
            }
        }


        fun saveImagePath(path: PictureBean){
            var kv = MMKV.defaultMMKV()
            kv.encode("ImagePath", path)
        }

        fun getSettings(){
            val kv = MMKV.defaultMMKV()
            if(kv.decodeBool(Constant.isUserSavedColor, false)){
                isUserSavedColor = true
                userSecondPageColor = kv.decodeInt(Constant.UserColor, 0)
            }
            isFirstNeedProcess = kv.decodeBool(Constant.isFirstNeedProcess, true)
            isSecondPageGradient = kv.decodeBool(Constant.isSecondPageGradient, true)
            isSecondPageBitmapGradient = kv.decodeBool(Constant.isSecondPageBitmapGradient,false)
            isSettingDone = true
        }

    }
}