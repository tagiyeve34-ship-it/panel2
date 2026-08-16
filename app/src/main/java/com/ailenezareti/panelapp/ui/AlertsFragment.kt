package com.ailenezareti.panelapp.ui
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ailenezareti.panelapp.R
import com.ailenezareti.panelapp.api.ApiClient
import com.ailenezareti.panelapp.databinding.FragmentAlertsBinding
import com.ailenezareti.panelapp.model.AlertEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class AlertsFragment:Fragment(),Refreshable{
 private var _b:FragmentAlertsBinding?=null;private val b get()=_b!!;private val a=A()
 override fun onCreateView(i:LayoutInflater,c:ViewGroup?,s:Bundle?):View{_b=FragmentAlertsBinding.inflate(i,c,false);return b.root}
 override fun onViewCreated(v:View,s:Bundle?){b.alertsRecycler.layoutManager=LinearLayoutManager(requireContext());b.alertsRecycler.adapter=a;refresh()}
 override fun refresh(){val ch=(activity as? MainActivity)?.activeChild()?:return;lifecycleScope.launch(Dispatchers.IO){try{val x=ApiClient.get(requireContext()).getAlerts(ch.id).body()?.alerts.orEmpty();launch(Dispatchers.Main){a.items=x;a.notifyDataSetChanged()}}catch(_:Exception){}}}
 override fun onDestroyView(){_b=null;super.onDestroyView()}
 class A:RecyclerView.Adapter<A.H>(){var items:List<AlertEntry> = emptyList();override fun onCreateViewHolder(p:ViewGroup,v:Int)=H(LayoutInflater.from(p.context).inflate(R.layout.item_alert,p,false));override fun getItemCount()=items.size;override fun onBindViewHolder(h:H,p:Int){val x=items[p];h.m.text=x.message;h.t.text=x.created_at}class H(v:View):RecyclerView.ViewHolder(v){val m:android.widget.TextView=v.findViewById(R.id.messageText);val t:android.widget.TextView=v.findViewById(R.id.timeText)}}
}
