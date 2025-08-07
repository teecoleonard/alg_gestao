package com.example.alg_gestao_02.ui.contrato

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.ByteArrayOutputStream

class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var paint: Paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private var path = Path()
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var savedSignatureData: ByteArray? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            // Se temos dados salvos da assinatura, restaurá-los
            if (savedSignatureData != null) {
                restoreSignatureFromData(savedSignatureData!!, w, h)
            } else {
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                canvas = Canvas(bitmap!!)
                canvas?.drawColor(Color.WHITE)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                path.moveTo(lastX, lastY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(lastX, lastY, (event.x + lastX) / 2, (event.y + lastY) / 2)
                lastX = event.x
                lastY = event.y
                canvas?.drawPath(path, paint)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(lastX, lastY)
                canvas?.drawPath(path, paint)
                path.reset()
                invalidate()
                return true
            }
        }
        return false
    }

    fun clear() {
        bitmap?.let {
            canvas?.drawColor(Color.WHITE)
            invalidate()
        }
        // Limpar dados salvos também
        savedSignatureData = null
    }

    fun getBitmap(): Bitmap? = bitmap

    /**
     * Salva os dados da assinatura atual para preservar durante mudanças de orientação
     */
    fun saveSignatureData(): ByteArray? {
        return bitmap?.let { bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val data = stream.toByteArray()
            savedSignatureData = data
            data
        }
    }

    /**
     * Restaura a assinatura a partir dos dados salvos
     */
    fun restoreSignatureFromData(data: ByteArray, width: Int, height: Int) {
        try {
            val savedBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            if (savedBitmap != null) {
                // Criar novo bitmap com as dimensões atuais
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                canvas = Canvas(bitmap!!)
                canvas?.drawColor(Color.WHITE)
                
                // Escalar e desenhar a assinatura salva no centro
                val scaleX = width.toFloat() / savedBitmap.width
                val scaleY = height.toFloat() / savedBitmap.height
                val scale = minOf(scaleX, scaleY)
                
                val scaledWidth = (savedBitmap.width * scale).toInt()
                val scaledHeight = (savedBitmap.height * scale).toInt()
                val x = (width - scaledWidth) / 2f
                val y = (height - scaledHeight) / 2f
                
                val scaledBitmap = Bitmap.createScaledBitmap(savedBitmap, scaledWidth, scaledHeight, true)
                canvas?.drawBitmap(scaledBitmap, x, y, null)
                
                savedBitmap.recycle()
                scaledBitmap.recycle()
                invalidate()
            }
        } catch (e: Exception) {
            // Se falhar ao restaurar, criar bitmap limpo
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap!!)
            canvas?.drawColor(Color.WHITE)
        }
    }

    /**
     * Restaura a assinatura a partir de dados salvos externamente
     */
    fun restoreFromSavedData(data: ByteArray?) {
        savedSignatureData = data
        if (data != null && width > 0 && height > 0) {
            restoreSignatureFromData(data, width, height)
        }
    }
} 