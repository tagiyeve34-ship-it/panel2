package com.ailenezareti.panelapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ailenezareti.panelapp.R
import com.ailenezareti.panelapp.api.ApiClient
import com.ailenezareti.panelapp.databinding.DialogHistoryBinding
import com.ailenezareti.panelapp.databinding.FragmentLocationBinding
import com.ailenezareti.panelapp.model.LocationPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class LocationFragment:Fragment(),Refreshable{
 private var _b:FragmentLocationBinding?=null;private val b get()=_b!!
 private var current:LocationPoint?=null;private var selected:LocationPoint?=null;private var routePoints:List<LocationPoint> = emptyList()
 companion object{private const val LAT="lat";private const val LON="lon";private const val TIME="time";fun newInstance(lat:Double,lon:Double,time:String)=LocationFragment().apply{arguments=Bundle().apply{putDouble(LAT,lat);putDouble(LON,lon);putString(TIME,time)}}}
 override fun onCreateView(i:LayoutInflater,c:ViewGroup?,s:Bundle?):View{Configuration.getInstance().load(requireContext(),PreferenceManager.getDefaultSharedPreferences(requireContext()));Configuration.getInstance().userAgentValue=requireContext().packageName;_b=FragmentLocationBinding.inflate(i,c,false);return b.root}
 override fun onViewCreated(v:View,s:Bundle?){b.mapView.setTileSource(TileSourceFactory.MAPNIK);b.mapView.setMultiTouchControls(true);b.mapView.controller.setZoom(16.0);b.historyButton.setOnClickListener{historyDialog()};b.recenterButton.setOnClickListener{focus(current)};b.googleMapsButton.setOnClickListener{openExternal(selected?:current)};refresh()}
 override fun refresh(){loadLatest()}
 private fun loadLatest(){val ch=(activity as? MainActivity)?.activeChild()?:return;lifecycleScope.launch(Dispatchers.IO){try{val p=ApiClient.get(requireContext()).getLocations(ch.id,"3h").body()?.locations?.firstOrNull();launch(Dispatchers.Main){if(_b==null)return@launch;if(p!=null){current=p;selected=null;drawLatestOnly(p)}}}catch(_:Exception){}}}
 private fun drawLatestOnly(p:LocationPoint){b.mapView.overlays.clear();addPin(p,"Son mövqe");showCard(p,"Son mövqe");focus(p);b.mapView.invalidate()}
 private fun historyDialog(){val d=DialogHistoryBinding.inflate(layoutInflater);val now=Calendar.getInstance();val start=(now.clone() as Calendar).apply{add(Calendar.HOUR_OF_DAY,-3)};var from=start.time;var to=now.time;val fmt=SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault());d.fromButton.setText(fmt.format(from));d.toButton.setText(fmt.format(to));val dlg=AlertDialog.Builder(requireContext()).setView(d.root).create();d.showRouteButton.setOnClickListener{val parsedFrom=try{fmt.parse(d.fromButton.text.toString())}catch(_:Exception){null};val parsedTo=try{fmt.parse(d.toButton.text.toString())}catch(_:Exception){null};if(parsedFrom==null||parsedTo==null||parsedFrom.after(parsedTo)){Toast.makeText(requireContext(),"Tarixi bu formada yaz: 16.08.2026 21:35",Toast.LENGTH_LONG).show();return@setOnClickListener};dlg.dismiss();loadHistory(parsedFrom,parsedTo,d.pointsSwitch.isChecked,d.routeSwitch.isChecked)};dlg.show()}
 private fun loadHistory(from:Date,to:Date,showPoints:Boolean,showRoute:Boolean){val ch=(activity as? MainActivity)?.activeChild()?:return;val apiFmt=SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);lifecycleScope.launch(Dispatchers.IO){try{val raw=ApiClient.get(requireContext()).getLocations(ch.id,"custom",apiFmt.format(from),apiFmt.format(to)).body()?.locations.orEmpty();val clean=cleanRoute(raw.reversed());launch(Dispatchers.Main){if(_b==null)return@launch;routePoints=clean;drawHistory(clean,showPoints,showRoute)}}catch(_:Exception){}}}
 private fun cleanRoute(src:List<LocationPoint>):List<LocationPoint>{if(src.size<2)return src;val out=mutableListOf<LocationPoint>();for(p in src){val lat=p.latitude.toDoubleOrNull()?:continue;val lon=p.longitude.toDoubleOrNull()?:continue;if(lat !in -90.0..90.0||lon !in -180.0..180.0)continue;if((p.accuracy_m?.toDoubleOrNull()?:0.0)>250)continue;if(out.isEmpty()){out+=p;continue};val prev=out.last();val dist=distance(prev,p);val dt=((parseTime(p.recorded_at)-parseTime(prev.recorded_at))/1000.0).coerceAtLeast(1.0);val speed=dist/dt*3.6;if(speed<=200 || dist<120){out+=p}}return out}
 private fun drawHistory(ps:List<LocationPoint>,showPoints:Boolean,showRoute:Boolean){b.mapView.overlays.clear();if(ps.isEmpty()){current?.let{drawLatestOnly(it)};Toast.makeText(requireContext(),"Bu intervalda məlumat yoxdur",Toast.LENGTH_SHORT).show();return};if(showRoute){val line=Polyline().apply{setPoints(ps.map{gp(it)});outlinePaint.color=ContextCompat.getColor(requireContext(),R.color.teal);outlinePaint.strokeWidth=5f};b.mapView.overlays.add(line)};if(showPoints){ps.forEachIndexed{i,p->val m=Marker(b.mapView).apply{position=gp(p);setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER);icon=ContextCompat.getDrawable(requireContext(),R.drawable.map_dot);setOnMarkerClickListener{_,_->selected=p;showCard(p,"Seçilmiş nöqtə");true}};b.mapView.overlays.add(m)};val step=max(1,ps.size/8);for(i in step until ps.lastIndex step step){val a=ps[i-1];val p=ps[i];val m=Marker(b.mapView).apply{position=gp(p);setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_CENTER);icon=ContextCompat.getDrawable(requireContext(),R.drawable.map_direction);rotation=bearing(a,p);setOnMarkerClickListener{_,_->selected=p;showCard(p,"Seçilmiş nöqtə");true}};b.mapView.overlays.add(m)}};addPin(ps.first(),"Başlanğıc");addPin(ps.last(),"Son nöqtə");selected=null;showCard(ps.last(),"Son nöqtə");val box=BoundingBox.fromGeoPoints(ps.map{gp(it)});b.mapView.post{b.mapView.zoomToBoundingBox(box,false,90)};b.mapView.invalidate()}
 private fun addPin(p:LocationPoint,label:String){b.mapView.overlays.add(Marker(b.mapView).apply{position=gp(p);setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);icon=ContextCompat.getDrawable(requireContext(),R.drawable.map_pin);setOnMarkerClickListener{_,_->selected=p;showCard(p,label);true}})}
 private fun showCard(p:LocationPoint,title:String){b.titleText.text=title;b.timeText.text="Son yenilənmə: ${p.recorded_at}";b.accuracyText.text="GPS dəqiqliyi: ${p.accuracy_m?:"—"} m";b.batteryText.text="${p.battery_pct?:0}%"}
 private fun focus(p:LocationPoint?){p?:return;b.mapView.controller.setCenter(gp(p));b.mapView.controller.setZoom(17.0)}
 private fun openExternal(p:LocationPoint?){p?:return;val lat=p.latitude;val lon=p.longitude;val geo=Uri.parse("geo:$lat,$lon?q=$lat,$lon");val i=Intent(Intent.ACTION_VIEW,geo);try{startActivity(i)}catch(_:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")))}}
 private fun gp(p:LocationPoint)=GeoPoint(p.latitude.toDouble(),p.longitude.toDouble())
 private fun distance(a:LocationPoint,b:LocationPoint):Double{val r=6371000.0;val la1=Math.toRadians(a.latitude.toDouble());val la2=Math.toRadians(b.latitude.toDouble());val dlat=la2-la1;val dlon=Math.toRadians(b.longitude.toDouble()-a.longitude.toDouble());val h=sin(dlat/2).pow(2)+cos(la1)*cos(la2)*sin(dlon/2).pow(2);return 2*r*asin(sqrt(h))}
 private fun bearing(a:LocationPoint,b:LocationPoint):Float{val lat1=Math.toRadians(a.latitude.toDouble());val lat2=Math.toRadians(b.latitude.toDouble());val dLon=Math.toRadians(b.longitude.toDouble()-a.longitude.toDouble());val y=sin(dLon)*cos(lat2);val x=cos(lat1)*sin(lat2)-sin(lat1)*cos(lat2)*cos(dLon);return ((Math.toDegrees(atan2(y,x))+360)%360).toFloat()}
 private fun parseTime(s:String):Long=try{SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).parse(s.replace('T',' ').take(19))?.time?:0}catch(_:Exception){0}
 override fun onResume(){super.onResume();b.mapView.onResume()};override fun onPause(){b.mapView.onPause();super.onPause()};override fun onDestroyView(){b.mapView.overlays.clear();_b=null;super.onDestroyView()}
}
