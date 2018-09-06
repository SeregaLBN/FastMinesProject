package fmg.android.utils;

/**
 * Приведение типов от платформо-независимых чистых Java классов fmg.common.geom.* к библиотечным android классам
 */
//object Cast {

fun  fmg.common.geom.Point      .toPoint      () : android.graphics.Point       { return android.graphics.Point      (this.x,            this.y); }
fun  fmg.common.geom.PointDouble.toPoint      () : android.graphics.PointF      { return android.graphics.PointF     (this.x.toFloat(),  this.y.toFloat()); }
fun android.graphics.Point      .toPoint      () :  fmg.common.geom.Point       { return  fmg.common.geom.Point      (this.x,            this.y); }
fun android.graphics.PointF     .toPointDouble() :  fmg.common.geom.PointDouble { return  fmg.common.geom.PointDouble(this.x.toDouble(), this.y.toDouble()); }

fun fmg.common.geom.Rect        .toRect       () : android.graphics.Rect        { return android.graphics.Rect      (this.x              , this.y             , this.right()           , this.bottom()); }
fun fmg.common.geom.RectDouble  .toRect       () : android.graphics.RectF       { return android.graphics.RectF     (this.x.toFloat()    , this.y.toFloat()   , this.right().toFloat() , this.bottom().toFloat()); }
fun android.graphics.Rect       .toRect       () :  fmg.common.geom.Rect        { return  fmg.common.geom.Rect      (this.left           , this.top           , this.width()           , this.height()); }
fun android.graphics.Rect       .toRectDouble () :  fmg.common.geom.RectDouble  { return  fmg.common.geom.RectDouble(this.left.toDouble(), this.top.toDouble(), this.width().toDouble(), this.height().toDouble()); }
fun android.graphics.RectF      .toRectDouble () :  fmg.common.geom.RectDouble  { return  fmg.common.geom.RectDouble(this.left.toDouble(), this.top.toDouble(), this.width().toDouble(), this.height().toDouble()); }

fun fmg.common.geom.Size        .toSize       () :    android.util.Size         { return    android.util.Size      (this.width                , this.height); }
fun fmg.common.geom.SizeDouble  .toSize       () :    android.util.SizeF        { return    android.util.SizeF     (this.width.toFloat()      , this.height.toFloat()); }
fun    android.util.Size        .toSize       () : fmg.common.geom.Size         { return fmg.common.geom.Size      (this.getWidth()           , this.getHeight()); }
fun    android.util.SizeF       .toSizeDouble () : fmg.common.geom.SizeDouble   { return fmg.common.geom.SizeDouble(this.getWidth().toDouble(), this.getHeight().toDouble()); }

fun fmg.common.geom.RegionDouble.toPolygon() : android.graphics.Path {
   val p = android.graphics.Path();
   var dot = this.getPoint(0);
   p.moveTo(dot.x.toFloat(), dot.y.toFloat());
   for (i in 1 until this.countPoints) {
      dot = this.getPoint(i);
      p.lineTo(dot.x.toFloat(), dot.y.toFloat());
   }
   p.close();
   return p;
}

fun List<fmg.common.geom.PointDouble>.toPolygon() : android.graphics.Path {
   val p = android.graphics.Path();
   var dot = this.get(0);
   p.moveTo(dot.x.toFloat(), dot.y.toFloat())
   for (i in 1 until this.size) {
      dot = this[i]
      p.lineTo(dot.x.toFloat(), dot.y.toFloat())
   }
   p.close();
   return p;
}

fun fmg.common.Color.toColor() :              Int { return (this.a and 0xFF shl 24) or (this.r and 0xFF shl 16) or (this.g and 0xFF shl 8) or (this.b and 0xFF); }
fun              Int.toColor() : fmg.common.Color { return fmg.common.Color(this); }

//}