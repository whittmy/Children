package children.lemoon.categrid;
 
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.util.LongSparseArray;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;



import com.dd.database.DatabaseManager;
import com.dd.database.QueryExecutor;
import com.dd.my.CateCourseMgrDAO;
import com.devsmart.android.ui.HorizontalListView;
import com.devsmart.android.ui.HorizontalListView.OnScrollListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.loopj.android.http.AsyncHttpClient;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import children.lemoon.Configer;
import children.lemoon.R;
import children.lemoon.music.MuPlayer;
import children.lemoon.myrespone.HeaderItemEntity;
import children.lemoon.myrespone.PlayItemEntity;
import children.lemoon.myrespone.ResponeResList;
import children.lemoon.player.org.Player;
import children.lemoon.reqbased.BaseReqActivity;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import children.lemoon.reqbased.entry.ResponsePager;
import children.lemoon.reqbased.utils.HttpManger;
import children.lemoon.ui.loading.CustomProgressDialog;
import children.lemoon.ui.view.BatteryImgView;
import children.lemoon.ui.view.BatteryRcvBindView;
import children.lemoon.utils.Logger;
import children.lemoon.utils.NetworkUtils;
import children.lemoon.utils.download.DownloadManagerPro;
import children.lemoon.utils.download.PreferencesUtils;
  
public class MoviesGridActivity extends BaseReqActivity implements View.OnClickListener, AdapterView.OnItemClickListener  {
	private MoviesGridAdapter adapter;
	private LinkedList<PlayItemEntity> data = new LinkedList<PlayItemEntity>();
	private View goBack;
	private int mCurrentPageIndex = 1;
	private int mPageSize = 21;
	private int mTotalPageCount = 9999;
	private TextView cataName;
	private int cataTypeId;
	private MyGridView cateGrid;
	
 	public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
	//public HorizontalListView mHListView;
	//private HorizontalScrollViewAdapter mAdapter;
	
	
	private PullToRefreshScrollView prvMovieList;
	ScrollView mScrollView;
	CustomProgressDialog mLoading;
	//SharedPreferences mPrefs;
	String mCacheRoot = null;
	
	
	public static final String DOWNLOAD_FOLDER_NAME = "Story_Courseware";
    private DownloadManagerPro downloadManagerPro;
	private DownloadManager mDlMgr;
 
    private MyHandler handler;

    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver completeReceiver;
	
	
	private boolean queryMovies() {
		if(!mLoading.isShowing())
			mLoading.show();
		HttpManger localHttpManger = new HttpManger(this, this.bHandler, this);
		HashMap<String, Object> localHashMap = new HashMap<String, Object>();
		localHashMap.put("id", String.valueOf(this.cataTypeId));
		localHashMap.put("pageindex", this.mCurrentPageIndex);
		localHashMap.put("pagesize", this.mPageSize);
		return localHttpManger.httpRequest(Configer.REQ_TYPE_CATEINFO, localHashMap, false, ResponeResList.class, false, false, true);
	}
	
	private int mLastIdx;
	private boolean mHReqing=false;
	private boolean queryHList(int lastidx, String id, int pageidx) {
		mHReqing = true;
		mLastIdx = lastidx;
		if(!mLoading.isShowing())
			mLoading.show();
		HttpManger localHttpManger = new HttpManger(this, this.bHandler, this);
		HashMap<String, Object> localHashMap = new HashMap<String, Object>();
		localHashMap.put("id", id);
		localHashMap.put("pageindex", pageidx);
		localHashMap.put("pagesize", this.mPageSize);
		return localHttpManger.httpRequest(Configer.REQ_TYPE_HLIST, localHashMap, false, ResponeResList.class, false, false, true);
	}
	
	
	public void onClick(View paramView) {
		finish();
	}


	
	
	private View getHeaderView(ViewGroup  root,boolean attachToRoot){
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); 
	//  LayoutInflater inflater1=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	//  LayoutInflater inflater2 = getLayoutInflater();
	    LayoutInflater inflater3 = LayoutInflater.from(this);
	    View view = inflater3.inflate(R.layout.act_cage_grid_cateheader, root,attachToRoot);
	    //view.setLayoutParams(lp);
	    return view;
	}
	
	
	
	protected void onPause() {
		super.onPause();
		mLoading.dismiss();
		unregisterReceiver(completeReceiver);  
		getContentResolver().unregisterContentObserver(downloadObserver);  

		//finish();
	}
	protected void onResume() {
		super.onResume();
		getContentResolver().registerContentObserver(  DownloadManagerPro.CONTENT_URI, true, downloadObserver);

		
		//发广播，关闭视频播放
		sendBroadcast(new Intent(Configer.Action.ACT_EXIT));
		
		
		
        /** register download success broadcast **/
        registerReceiver(completeReceiver, new IntentFilter( DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        
        //updateView();
        
		registerReceiver(batteryReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED")); 
		
	}
	
	
	HashMap<String,Long> mCourseAndDownIdMap = new HashMap<String,Long>(); //保存课程id与其下载id的对应关系
	LongSparseArray<View> mDownIdAndViewMap = new LongSparseArray<View>(); //保存下载id与其对应的进度view的对应关系
	
	private BatteryRcvBindView batteryReceiver;
	
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		unregisterReceiver(batteryReceiver);
	}
	
	//pcontainer;
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.act_cate_grid);

		mLoading =  CustomProgressDialog.createDialog(this);
		mLoading.show();
		
		batteryReceiver = new BatteryRcvBindView((BatteryImgView)findViewById(R.id.battery));


		DatabaseManager.initializeInstance(this);
		DatabaseManager.getInstance().executeQuery(new QueryExecutor() { //同步执行
			@Override
			public void run(SQLiteDatabase database) {
				// TODO Auto-generated method stub
				CateCourseMgrDAO udao = new CateCourseMgrDAO(database, MoviesGridActivity.this); // your class
				Cursor cursor = udao.selectAll();
				if(cursor == null){
		    		Logger.LOGD("", "cate have no data to ...");
		    		return;
		    	}
				
				for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
 					 String courseid = cursor.getString(cursor.getColumnIndex(CateCourseMgrDAO.COLUMNS_CSID));
					 long downid = cursor.getLong(cursor.getColumnIndex(CateCourseMgrDAO.COLUMNS_DWID));
					 mCourseAndDownIdMap.put(courseid, downid);
 		    	} 
				cursor.close();
			}
		});
		
		this.goBack = findViewById(R.id.go_back);
		this.goBack.setOnClickListener(this);		
 
		handler = new MyHandler();
		mDlMgr = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);  
        downloadManagerPro = new DownloadManagerPro(mDlMgr);
		downloadObserver = new DownloadChangeObserver();
        completeReceiver = new CompleteReceiver();
        
        
		//mPrefs = PreferenceManager.getDefaultSharedPreferences(this);  
		mCacheRoot = StorageUtils.getCacheDirectory(this).getAbsolutePath();
		
		cfgImgLoader();
		this.cataName = ((TextView) findViewById(R.id.tv_movie_type));
		this.cateGrid = ((MyGridView) findViewById(R.id.gv_movies_list));
		this.cateGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		this.prvMovieList = ((PullToRefreshScrollView) findViewById(R.id.prv_vr_movies));
		
		prvMovieList.setHeaderState(false);
		prvMovieList.setOnRefreshListener(new OnRefreshListener2<ScrollView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				prvMovieList.onRefreshComplete();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
				// TODO Auto-generated method stub
				if (mCurrentPageIndex <= mTotalPageCount) {
					queryMovies();
					return;
				}
				else{
					//不用刷新了，因为已经最后一页了
					prvMovieList.onRefreshComplete();
				}
			}
		});

		mScrollView = prvMovieList.getRefreshableView();
		
		
		//this.prvMovieList.setOnFooterRefreshListener(this);
		
		//取参数
		String str = getIntent().getStringExtra("curCata");
		this.cataName.setText(str);
		this.cataTypeId = getIntent().getIntExtra("cataId", 0);

		
		
		cateGrid.setNumColumns(3);
		cateGrid.setHaveScrollbar(false);

		this.adapter = new MoviesGridAdapter(this, this.data, mLoader);
		this.cateGrid.setAdapter(this.adapter);
		this.cateGrid.setOnItemClickListener(this);
		queryMovies();
	}
 
 
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
		Adapter adp1 =  paramAdapterView.getAdapter();
		PlayItemEntity pie = (PlayItemEntity)adp1.getItem(paramInt);
		
		if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "当前网络不可用", Toast.LENGTH_SHORT).show();
			return;
		}
 
//		if(paramInt == 1){
//			pie = new PlayItemEntity();
//			pie.setId(1324);
//			pie.setType(10);
//			pie.setName("test1");
//			pie.setDownUrl("http://app.znds.com/down/20150706/TJ-3.0.6-dangbei.apk");
//		}
//		else if(paramInt == 2){
//			pie = new PlayItemEntity();
//			pie.setId(5545);
//			pie.setType(10);
//			pie.setName("test2");
//			pie.setDownUrl("http://app.znds.com/down/20150602/dsm-2.5.6-dangbei.apk");			
//		}
//		else if(paramInt == 3){
//			pie = new PlayItemEntity();
//			pie.setId(33424);
//			pie.setType(10);
//			pie.setName("test3");
//			pie.setDownUrl("http://app.znds.com/down/20150608/vstqjh-2.6.7.2-dangbei.apk");			
//		} 		
		
		if(pie.getType() == 5){
			//课件
			final String courseid = pie.getIds();
			HScrollAdapter adp = (HScrollAdapter)adp1;
			String idstr = String.valueOf(courseid);

			File f = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
			if(!f.exists())
				f.mkdir();
			String path = f.getAbsolutePath()+"/";
 
			//如果文件不存在
			if(!new File(path+idstr).exists()){	  //?????
	    		if(adp.getIdx() >= 0){
	    			//toast提示
	    			//MyToast.makeText(MoviesGridActivity.this, "正在下载课件，耐心点儿^_^").show();
	    			
	    			//Crouton.makeText(MoviesGridActivity.this, "正在下载课件，耐心点儿^_^", Style.INFO).show();
	    			
	    			
	    			//Crouton.cancelAllCroutons();
	    			
	    			
	    			HorizontalListView hv = mHlistArr.get(adp.getIdx()) ;
	    			//在ListView中，使用getChildAt(index)的取值，只能是当前可见区域（列表可滚动）的子项！ 即取值范围在 >= ListView.getFirstVisiblePosition() &&  <= ListView.getLastVisiblePosition(); 
	    			Logger.LOGD("", "pos="+paramInt+", firstvispos="+hv.getFirstVisiblePosition()+", rslt="+(paramInt-hv.getFirstVisiblePosition()));
	     			View v = hv.getChildAt(paramInt-hv.getFirstVisiblePosition());
	     			final View v1 = v.findViewById(R.id.dlprogress);  //完成时要清除掉
	     			if(v1.getTag() != null){
	     				//已经下载
	     				return;
	     			}
	     			
		            //开始下载   
		            Uri resource = Uri.parse(encodeGB(pie.getDownUrl()));   
		            DownloadManager.Request request = new DownloadManager.Request(resource);   
		            request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);   
		            request.setAllowedOverRoaming(false);   
	 
		            //不在通知栏中显示   
		            request.setShowRunningNotification(false);  
		            request.setVisibleInDownloadsUi(false);  
		            
		            //sdcard的目录下的 某文件夹  
		            request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, idstr);  
		            request.setTitle(pie.getName());   
		            final long downloadId  = mDlMgr.enqueue(request);  
		            v1.setTag(downloadId);
		            
		            PreferencesUtils.putString(getApplicationContext(), "dl_"+downloadId, courseid);  //方便downloadid=>couseid
		            
		            //保存数据库
		            DatabaseManager.getInstance().executeQuery(new QueryExecutor() {
						@Override
						public void run(SQLiteDatabase database) {
							// TODO Auto-generated method stub
							CateCourseMgrDAO udao = new CateCourseMgrDAO(database, MoviesGridActivity.this); // your class
							udao.insert(courseid, downloadId);
							
				            mCourseAndDownIdMap.put(courseid, downloadId);
				            mDownIdAndViewMap.put(downloadId, v1);
						}
		            });

	                updateView(downloadId);
	    		}				
			}
			else
			{
				//打开
				Intent it = new Intent();
				ComponentName com= new ComponentName("flexplayer.lemoon", "flexplayer.lemoon.MainActivity");  
				it.setComponent(com);  
				it.putExtra("courseId", /*14060*/courseid);								
				startActivity(it);
				return;
			}
		}
		//音频 & 音频类别
		else if(pie.getType() == 0 || pie.getType()==10){
			//本地测试
//			Intent it = new Intent();  
//			it.setComponent(new ComponentName("children.lemoon", "children.lemoon.music.MuPlayer")); 
//			it.putExtra("curCata",pie.getName());
//			it.putExtra("localpath", "/mnt/extsd/音乐");
//			startActivity(it);  
			
			//网络
			Intent it = new Intent(this, MuPlayer.class);
			it.putExtra("cataId", pie.getIds());
			it.putExtra("curCata",pie.getName());
			it.putExtra("type", pie.getType());
			startActivity(it);			
		}
		else if(pie.getType() == 4){
			//本地测试
//			Intent it = new Intent();  
//			it.setComponent(new ComponentName("children.lemoon", "children.lemoon.player.org.Player")); 
//			it.putExtra("curCata",pie.getName());
//			it.putExtra("localpath", "/mnt/extsd/动画");
//			startActivity(it);  			
			
			//网络
			Intent it = new Intent(this, Player.class);
			it.putExtra("cataId", pie.getIds());
			it.putExtra("type", pie.getType());
			startActivity(it);
		}
	}

 
	
	/** 
     * 如果服务器不支持中文路径的情况下需要转换url的编码。 
     * @param string 
     * @return 
     */  
    public String encodeGB(String string)  
    {  
        //转换中文编码  
        String split[] = string.split("/");  
        for (int i = 1; i < split.length; i++) {  
            try {  
                split[i] = URLEncoder.encode(split[i], "GB2312");  
            } catch (UnsupportedEncodingException e) {  
                e.printStackTrace();  
            }  
            split[0] = split[0]+"/"+split[i];  
        }  
        split[0] = split[0].replaceAll("\\+", "%20");//处理空格  
        return split[0];  
    }  
	

	  
	LinkedList<HorizontalListView> mHlistArr = new LinkedList<HorizontalListView>(); 
	LinkedList<HScrollAdapter> mHAdpArr = new LinkedList<HScrollAdapter>();
	protected void onPostHandle(int paramInt1, Object paramObject1, boolean paramBoolean, int paramInt2, Object paramObject2, Object paramObject3) {
		super.onPostHandle(paramInt1, paramObject1, paramBoolean, paramInt2, paramObject2, paramObject3);
		if (paramObject1 == null) {
 
		}
		else if (paramInt1 == Configer.REQ_TYPE_CATEINFO) {
			ResHeadAndBody localResHeadAndBody = (ResHeadAndBody) paramObject1;
			ResponeResList list = (ResponeResList) localResHeadAndBody.getBody();
			
			//先刷新全部的
			if((list.getResList()!=null && list.getResList().size() > 0)){
				this.data.addAll(list.getResList());
				Logger.LOGD("====================notifyDataSetChanged 1");
				this.adapter.notifyDataSetChanged();
			}
			
			//再头部的，否则有问题。
			boolean bhadHeader = false;
			if(list.getHeaderList()!=null && list.getHeaderList().size()>0 && mCurrentPageIndex == 1){
				mHlistArr.clear();
				mHAdpArr.clear();
				
				LinearLayout pcontainer = (LinearLayout)MoviesGridActivity.this.findViewById(R.id.headercontainer);
				List<HeaderItemEntity> hList = list.getHeaderList();
				
				if(hList!=null && hList.size()>0){
					int i = hList.size()-1;
					bhadHeader = true;
					for(; i>=0; i--){
						HeaderItemEntity h = hList.get(i);
						LinkedList<PlayItemEntity> childlist = new LinkedList<PlayItemEntity>(h.getChildList());
						if(childlist.size() == 0)
							continue;
						
						View v = getHeaderView(pcontainer, false);
						TextView tx = (TextView)v.findViewById(R.id.header_title);
						tx.setText(h.getTitle());

						HScrollAdapter adp = new HScrollAdapter(MoviesGridActivity.this, childlist, mLoader, mCourseAndDownIdMap, mDownIdAndViewMap, h.getStyle());
						adp.setIdx(i); //setidx,用于其它地方定位"是哪个类别"
						adp.setTypeId(h.getIds()); // settypeid 用于再次请求，否则怎么去取下一页了，因为“类别”是必须的
						mHAdpArr.addFirst(adp);

						HorizontalListView hv = (HorizontalListView)v.findViewById(R.id.header_grid);
						hv.setTag(i); //!!!!!!!! i保存了hview的序号，对应adapter，根据view获取i，然后便可以获取对应的adapter
						hv.setAdapter(adp);
						
						
						hv.setOnItemClickListener(MoviesGridActivity.this);
						hv.setOnScrollListener(new OnScrollListener() {
							@Override
							public void onScrollStateChanged(View view, int scrollState) {
								// TODO Auto-generated method stub
							}
							
							@Override
							public void onScroll(View view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
								// TODO Auto-generated method stub
								//Logger.LOGD("", "onScroll:  "+ firstVisibleItem+","+visibleItemCount+","+totalItemCount);

								if(view != null ){
									//Logger.LOGD("", "viewtg="+(Integer)view.getTag());
									int idx = (Integer)view.getTag();
									if(idx<0 || idx >= mHAdpArr.size()){
										return;
									}
									
									HScrollAdapter adp = mHAdpArr.get(idx);
									if(!mHReqing && firstVisibleItem+visibleItemCount+5 >= totalItemCount && adp.getCount()>mPageSize){
										String id = adp.getTypeId();
										int pgidx = adp.getPgIdx();
										queryHList(idx, id, pgidx+1);
									}
								}
							}
						});
						mHlistArr.addFirst(hv);
						pcontainer.addView(v, 0);	
						adp.notifyDataSetChanged();
					}
 				}
			}
	
			//注意，如果下面的部分整个在头部之前也有问题。
  			if((list.getResList()!=null && list.getResList().size() > 0)){
//				this.data.addAll(list.getResList());
//				this.adapter.notifyDataSetChanged();
				
				//lable_all
				MoviesGridActivity.this.findViewById(R.id.lable_all).setVisibility(View.VISIBLE); 
				MoviesGridActivity.this.findViewById(R.id.maingrid_header_title_bg).setVisibility(View.VISIBLE); 
				MoviesGridActivity.this.findViewById(R.id.grid_cateall_bg_bottom).setVisibility(View.VISIBLE);
				
				//该句意思是这是最后一页了。
				if(list.getResList().size()<mPageSize){
					mTotalPageCount = mCurrentPageIndex;
					
					String str = "没有啦！^_^";
					prvMovieList.setPullLabel(str);
					prvMovieList.setRefreshingLabel(str);
					prvMovieList.setReleaseLabel(str);
				}
				
				
				this.mCurrentPageIndex ++;
			}
  			else{
  				if(mCurrentPageIndex == 1 && bhadHeader){
  					//根本第一页就没有取到数据嘛
  					mTotalPageCount = 0;
  				}
  				else{
  					mTotalPageCount = mCurrentPageIndex;
  					mCurrentPageIndex ++;
  				}
  				
				String str = "没有啦！^_^";
				prvMovieList.setPullLabel(str);
				prvMovieList.setRefreshingLabel(str);
				prvMovieList.setReleaseLabel(str);
  			}
  			
  				

		} else if ((paramInt1 == Configer.REQ_TYPE_HLIST)) {
			ResHeadAndBody localResHeadAndBody = (ResHeadAndBody)paramObject1;
			ResponeResList list = (ResponeResList) localResHeadAndBody.getBody();
			if(mLastIdx<0 || mLastIdx >= mHAdpArr.size()){
				mHReqing = false;
			}
			else{
				HScrollAdapter adp = mHAdpArr.get(mLastIdx);
				adp.addData(list.getResList());
				adp.notifyDataSetChanged();
				Logger.LOGD("====================notifyDataSetChanged 3");
				adp.setPgIdx(adp.getPgIdx()+1);
				mHReqing = false;
			}
		}
		
		mLoading.cancel();
		prvMovieList.onRefreshComplete();
		return;
	}
 
 //////////////////////////////////////////
    public void updateView(long downloadId) {
        int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
        handler.sendMessage(handler.obtainMessage(777, (int)downloadId, 0,(Object)bytesAndStatus/*bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]*/));
    }
    
 
	 class DownloadChangeObserver extends ContentObserver {
	        public DownloadChangeObserver() {
	            super(handler);
	        }
	        
	        public boolean isNumeric(String str){
	        	  for (int i = str.length();--i>=0;){   
	        	   if (!Character.isDigit(str.charAt(i))){
	        	    return false;
	        	   }
	        	  }
	        	  return true;
	        }

	        @Override
	        public void onChange(boolean selfChange, Uri uri) {
	        // TODO Auto-generated method stub
	        	//super.onChange(selfChange, uri);
	        	Logger.LOGD("", "onchange: uri="+uri);
	        	String url = uri.toString();
	        	String id = url.substring(url.lastIndexOf("/")+1);
	        	if(isNumeric(id)){
	        		Logger.LOGD("", "getdlid="+id);
	        		updateView(Integer.valueOf(id));
	        	}
	        }
	 
	    }
	 
	    class CompleteReceiver extends BroadcastReceiver {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            /**
	             * get the id of download which have download success, if the id is
	             * my id and it's status is successful, then install it
	             **/
	            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//	            if (completeDownloadId == downloadId) {
//	                updateView(completeDownloadId);
//	                // if download successful  
//	                if (downloadManagerPro.getStatusById(downloadId) == DownloadManager.STATUS_SUCCESSFUL) {
//	                		
//	                }
//	            }
	        }
	    };
 
	    @SuppressLint("HandlerLeak")
	    private class MyHandler extends Handler {
	        @Override
	        public void handleMessage(Message msg) {
	            super.handleMessage(msg);
	 
	            switch (msg.what) {
	            case 777:
	            	final long downid = msg.arg1;
	                ProgressBar downloadProgress = (ProgressBar)mDownIdAndViewMap.get(downid);
	                if(downloadProgress == null)
	                	break;
	                
	            	int[]info = (int[])msg.obj;
	            	int status = info[2];
	                if (isDownloading(status)) {
	                    downloadProgress.setVisibility(View.VISIBLE);
	                    downloadProgress.setMax(0);
	                    downloadProgress.setProgress(0);

	                    if (info[1] < 0) {
	                        downloadProgress.setIndeterminate(true);
	                    } else {
	                        downloadProgress.setIndeterminate(false);
	                        downloadProgress.setMax(info[1]);
	                        downloadProgress.setProgress(info[0]);
	                    }
	                }
	                else {
	                    downloadProgress.setVisibility(View.GONE);
	                    downloadProgress.setMax(0);
	                    downloadProgress.setProgress(0);
	                    
	                    
	                    final String courseid =  PreferencesUtils.getString(getApplicationContext(), "dl_"+downid);
	                    PreferencesUtils.delKey(MoviesGridActivity.this, "dl_"+downid);
	                    
	                    if (status == DownloadManager.STATUS_SUCCESSFUL 
	                    		|| status == DownloadManager.STATUS_FAILED) {
	                		DatabaseManager.getInstance().executeQuery(new QueryExecutor() { //同步执行
	                			@Override
	                			public void run(SQLiteDatabase database) {
	                				// TODO Auto-generated method stub
	                				//清除痕迹
	                				CateCourseMgrDAO udao = new CateCourseMgrDAO(database, MoviesGridActivity.this); // your class
	                				udao.deleteByDownId(downid);
	                				
	                				if(courseid != null){
	                					//mCourseAndDownIdMap.delete(courseid);
	                					mCourseAndDownIdMap.remove(courseid);
	                				}
	   
	                				View v = mDownIdAndViewMap.get(downid);
	                				if(v!=null)
	                					v.setTag(null);
	                				mDownIdAndViewMap.delete(downid);	                				
	                			}
	                		});
	                		
	                		if(status == DownloadManager.STATUS_FAILED){
	                			//删除文件
	                			String path = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME).getAbsolutePath()+"/";
	                			File f = new File(path+courseid);
	                			//如果文件存在
	                			if(f.exists()){	
	                				f.delete();
	                			}
	                		}
	                		
	                    } else {
	                    	
	                    }
	                }
	                break;
	            }
	        }
	    }
	 
	    static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");
	 
	    public static final int MB_2_BYTE = 1024 * 1024;
	    public static final int KB_2_BYTE = 1024;
	 
	    /**
	     * @param size
	     * @return
	     */
	    public static CharSequence getAppSize(long size) {
	        if (size <= 0) {
	            return "0M";
	        }
	 
	        if (size >= MB_2_BYTE) {
	            return new StringBuilder(16).append(
	                    DOUBLE_DECIMAL_FORMAT.format((double) size / MB_2_BYTE))
	                    .append("M");
	        } else if (size >= KB_2_BYTE) {
	            return new StringBuilder(16).append(
	                    DOUBLE_DECIMAL_FORMAT.format((double) size / KB_2_BYTE))
	                    .append("K");
	        } else {
	            return size + "B";
	        }
	    }
	 
	    public static String getNotiPercent(long progress, long max) {
	        int rate = 0;
	        if (progress <= 0 || max <= 0) {
	            rate = 0;
	        } else if (progress > max) {
	            rate = 100;
	        } else {
	            rate = (int) ((double) progress / max * 100);
	        }
	        return new StringBuilder(16).append(rate).append("%").toString();
	    }
	 
	    public static boolean isDownloading(int downloadManagerStatus) {
	        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
	                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
	                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
	    }	
	
	
	
	
	////////////////////////////////////////

	public void onVoiceCommand(int paramInt) {
	}

	ImageLoader mLoader;
	private void cfgImgLoader(){
		mLoader = ImageLoader.getInstance();
		File cacheDir = new File(mCacheRoot+"/pics/");	// ;
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
		        .memoryCacheExtraOptions(112, 130) // default = device screen dimensions
		        .diskCacheExtraOptions(112, 130, null)
		        .threadPoolSize(3) // default 3
		        .threadPriority(Thread.NORM_PRIORITY - 2) // default
		        .tasksProcessingOrder(QueueProcessingType.FIFO) // default
		        .denyCacheImageMultipleSizesInMemory()
		        //.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
		        //.memoryCacheSize(2 * 1024 * 1024)
		        //.memoryCacheSizePercentage(13) // default
		        .diskCache(new UnlimitedDiskCache(cacheDir)) // default,  设置带时限的文件缓存是不和要求的，如果我获得不了之前文件，哪怕其过期了，我也删除不了
		        .diskCacheSize(500 * 1024 *1024)		//500M    			//所以缓存啊，还是我定期去清理
		        .diskCacheFileCount(10000)			//10000 pics    
		        //.writeDebugLogs()				// Logger.LOGD()
		        .defaultDisplayImageOptions(new Builder()
		        								.cacheOnDisc(true)
		        								.cacheOnDisk(true)
		        								//.cacheInMemory(true)
		        								.showImageForEmptyUri(Configer.Res.get_icon_for_categrid())
		        								.showImageOnLoading(Configer.Res.get_icon_for_categrid())
		        								.showImageOnFail(Configer.Res.get_icon_for_categrid())
		        								.build())
		        .build();
		mLoader.init(config);
	}
}
