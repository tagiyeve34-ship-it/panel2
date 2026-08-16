package com.ailenezareti.panelapp.ui
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ailenezareti.panelapp.api.ApiClient
import com.ailenezareti.panelapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class HomeFragment:Fragment(),Refreshable{
 private var _b:FragmentHomeBinding?=null; private val b get()=_b!!
 override fun onCreateView(i:LayoutInflater,c:ViewGroup?,s:Bundle?):View{_b=FragmentHomeBinding.inflate(i,c,false);return b.root}
 override fun onViewCreated(v:View,s:Bundle?){b.openMapButton.setOnClickListener{(activity as? MainActivity)?.openLocationTab()};refresh()}
 override fun refresh(){val ch=(activity as? MainActivity)?.activeChild()?:return;lifecycleScope.launch(Dispatchers.IO){try{val r=ApiClient.get(requireContext()).getLocations(ch.id,"3h");val p=r.body()?.locations?.firstOrNull();launch(Dispatchers.Main){if(_b==null)return@launch;b.statusText.text=if(p==null)"Son GPS məlumatı yoxdur" else "● Son GPS: ${p.recorded_at}\n\nBatareya: ${p.battery_pct?:0}%\nGPS dəqiqliyi: ${p.accuracy_m?:"—"} m"}}catch(_:Exception){}}}
 override fun onDestroyView(){_b=null;super.onDestroyView()}
}
