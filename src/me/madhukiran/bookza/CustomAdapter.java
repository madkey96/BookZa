package me.madhukiran.bookza;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/********* Adapter class extends with BaseAdapter and implements with OnClickListener ************/
public class CustomAdapter extends BaseAdapter   implements OnClickListener {
          
         /*********** Declare Used Variables *********/
         private Activity activity;
         private ArrayList<book> data;
         private static LayoutInflater inflater=null;
         public Resources res;
         book tempValues=null;
         int i=0;
         int lastPosition=0;
          
         /*************  CustomAdapter Constructor *****************/
         public CustomAdapter(Activity a, ArrayList<book> d,Resources resLocal) {
              
                /********** Take passed values **********/
                 activity = a;
                 data=d;
                 res = resLocal;
              
                 /***********  Layout inflator to call external xml layout () ***********/
                  inflater = ( LayoutInflater )activity.
                                              getSystemService(Context.LAYOUT_INFLATER_SERVICE);
              
         }
      
         /******** What is the size of Passed Arraylist Size ************/
         public int getCount() {
              
             if(data.size()<=0)
                 return 1;
             return data.size();
         }
      
         public Object getItem(int position) {
             return position;
         }
      
         public long getItemId(int position) {
             return position;
         }
          
         /********* Create a holder Class to contain inflated xml file elements *********/
         public static class ViewHolder{
              
             public TextView name;
             public TextView author;
             public TextView type;
             public TextView size;
             public ProgressBar mProgressbar1;
      
         }
      
         /****** Depends upon data size called for each row , Create each ListView row *****/
         public View getView(int position, View convertView, ViewGroup parent) {
              
             View vi = convertView;
             ViewHolder holder;
              
             if(convertView==null){
                  
                 /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
                 vi = inflater.inflate(R.layout.list_item, null);
                  
                 /****** View Holder Object to contain tabitem.xml file elements ******/
 
                 holder = new ViewHolder();
                 holder.name = (TextView) vi.findViewById(R.id.name);
                 holder.author=(TextView)vi.findViewById(R.id.author);
                 holder.size=(TextView)vi.findViewById(R.id.size);
                 holder.type=(TextView)vi.findViewById(R.id.type);
                 holder.mProgressbar1 = (ProgressBar)vi.findViewById(R.id.progressBar1); 
                /************  Set holder with LayoutInflater ************/
                 vi.setTag( holder );
             }
             else 
                 holder=(ViewHolder)vi.getTag();
              
             if(data.size()<=0)
             {
                 holder.name.setText("No Such File Found");
                 holder.author.setText("Try using author's name");
                 holder.type.setVisibility(View.INVISIBLE);
                 holder.size.setVisibility(View.INVISIBLE);
                 holder.mProgressbar1.setVisibility(View.GONE);
             }
             else
             {
                 /***** Get each Model object from Arraylist ********/
                 tempValues=null;
                 tempValues = ( book ) data.get( position );
                  
                 /************  Set Model values in Holder elements ***********/
 
                  holder.name.setText( tempValues.getName() );
                  holder.author.setText( tempValues.getAuthors() );
                  holder.size.setText(tempValues.getSize());
                  holder.type.setText(tempValues.getType());
                  holder.type.setVisibility(View.VISIBLE);
                  holder.size.setVisibility(View.VISIBLE);
                  if (tempValues.progress==0 || tempValues.progress==100) {
                	  holder.mProgressbar1.setVisibility(View.GONE);
                  } 
                  else {
                	  holder.mProgressbar1.setVisibility(View.VISIBLE);
                	  holder.mProgressbar1.setProgress(tempValues.getProgress());
                  }
//                   holder.image.setImageResource(
//                               res.getIdentifier(
//                               "com.androidexample.customlistview:drawable/"+tempValues.getImage()
//                               ,null,null));
                   
                  /******** Set Item Click Listner for LayoutInflater for each row *******/
 
                  vi.setOnClickListener(new OnItemClickListener( position ));
             }
             Animation animation = AnimationUtils.loadAnimation(activity, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
             vi.startAnimation(animation);
             lastPosition = position;
             return vi;
         }
          
         @Override
         public void onClick(View v) {
                 Log.v("CustomAdapter", "=====Row button clicked=====");
         }
          
         /********* Called when Item click in ListView ************/
         private class OnItemClickListener  implements OnClickListener{          
             private int mPosition;
              
             OnItemClickListener(int position){
                  mPosition = position;
             }
              
             @Override
             public void onClick(View arg0) {
 
        
               MainActivity sct = (MainActivity)activity;
 
              /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
 
                 sct.onItemClick(mPosition);
             }              
         }  
     }
