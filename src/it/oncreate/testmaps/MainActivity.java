package it.oncreate.testmaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	GoogleMap mMap;
	static final LatLng ANCONA = new LatLng(43.6158572, 13.5187567);
	
	protected Path path;
	
	ImageView drawable;
	Bitmap bitmap;
	Canvas canvas;
	Paint paint;
	
	double maxnord;
	double maxsud;
	double maxest;
	double maxovest;
	
	float startX;
	float startY;
	float prevX;
	float prevY;
	
	ImageButton search;
	ImageButton clear;
	ImageButton cancel;
	
	ArrayList<LatLng> points = new ArrayList<LatLng>();
	it.oncreate.testmaps.sromku.Polygon.Builder android_polygon_builder;
	it.oncreate.testmaps.sromku.Polygon android_polygon;
	
	ArrayList<Polygon> poligoni_su_mappa = new ArrayList<Polygon>();
	ArrayList<Marker> marker_su_mappa = new ArrayList<Marker>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// enable full screen application
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // set xml layout
        setContentView(R.layout.activity_main);
        
        maxnord = 0;
    	maxsud = 0;
    	maxest = 0;
    	maxovest = 0;
		
		// Before you can interact with a GoogleMap object,
		// you will need to confirm that an object can be instantiated.
		setUpMapIfNeeded();
		
		drawable = (ImageView) findViewById(R.id.viewOnMap);
		drawable.setVisibility(View.GONE);
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		final int screenWidth = displaymetrics.widthPixels;
		final int screenHeight = displaymetrics.heightPixels;
		
		search = (ImageButton) findViewById(R.id.search);
		clear = (ImageButton) findViewById(R.id.clear);
		cancel = (ImageButton) findViewById(R.id.cancel);
		cancel.setVisibility(View.GONE);
		
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
		        canvas = new Canvas(bitmap);
		        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		        paint.setColor(Color.WHITE);
		        paint.setStrokeWidth(4);
		        drawable.setImageBitmap(bitmap);
		        
		        android_polygon_builder = it.oncreate.testmaps.sromku.Polygon.Builder();
				
				cancel.setVisibility(View.VISIBLE);
				drawable.setVisibility(View.VISIBLE);
				clear.setVisibility(View.GONE);
				search.setVisibility(View.GONE);
				
				drawable.bringToFront();
				cancel.bringToFront();
			}
		});
		
		clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for(int i = 0; i < poligoni_su_mappa.size(); i++){
					poligoni_su_mappa.get(i).remove();
				}
				poligoni_su_mappa.clear();
				for(int i = 0; i < marker_su_mappa.size(); i++){
					marker_su_mappa.get(i).remove();
				}
				marker_su_mappa.clear();
			}
		});
		
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel.setVisibility(View.GONE);
				drawable.setVisibility(View.GONE);
				search.setVisibility(View.VISIBLE);
				clear.setVisibility(View.VISIBLE);
			}
		});
		
		drawable.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				android.graphics.Point p = new android.graphics.Point();

				switch (action) {
					case MotionEvent.ACTION_DOWN: // eseguita al primo touch sul drawable
						try {
							startX = event.getX();
						    startY = event.getY();
						    p.x = (int) startX;
						    p.y = (int) startY;
						    LatLng latlngpoint  = mMap.getProjection().fromScreenLocation(p);
						    points.add(latlngpoint);

						    // latitudine
						    maxnord = latlngpoint.latitude;
					    	maxsud = latlngpoint.latitude;
					    	
					    	// longitudine
					    	maxest = latlngpoint.longitude;
					    	maxovest = latlngpoint.longitude;
					    	
					    	// imposta prev come start
					    	prevX = startX;
					    	prevY = startY;
					    	
					    	// aggiungi al poligono virtuale
					    	android_polygon_builder.addVertex(new it.oncreate.testmaps.sromku.Point(startX, startY));
						} catch (Exception e) {
							Toast.makeText(MainActivity.this, "Area non definita!", Toast.LENGTH_SHORT).show();
						}
				    	
					    break;
					case MotionEvent.ACTION_MOVE:	// eseguita ad ogni spostamento sul drawable
						try {
							float actualX = event.getX();
							float actualY = event.getY();
							
							p.x = (int) actualX;
						    p.y = (int) actualY;
						    LatLng latlngpoint1  = mMap.getProjection().fromScreenLocation(p);
						    points.add(latlngpoint1);
						    
						    //Canvas.drawLine(start_x, start_y, end_x, end_y, paint_obj);
						    canvas.drawLine(prevX, prevY, actualX, actualY, paint);
						    drawable.invalidate();

						    // latitudine
						    if(latlngpoint1.latitude > maxnord) maxnord = latlngpoint1.latitude;
						    if(latlngpoint1.latitude < maxsud) maxsud = latlngpoint1.latitude;
					    	
					    	
					    	// longitudine
						    if(latlngpoint1.longitude > maxest) maxest = latlngpoint1.longitude;
						    if(latlngpoint1.longitude < maxovest) maxovest = latlngpoint1.longitude;
						    
						    // imposta prev come actual
					    	prevX = actualX;
					    	prevY = actualY;
					    	
					    	// aggiungi al poligono virtuale
					    	android_polygon_builder.addVertex(new it.oncreate.testmaps.sromku.Point(actualX, actualY));
						} catch (Exception e) { ; }
					    break;
					case MotionEvent.ACTION_UP:	// eseguita quando termina la gesture e il dito non è più sullo schermo
						try {
							float endX = event.getX();
						    float endY = event.getY();
						    p.x = (int) endX;
						    p.y = (int) endY;
						    LatLng latlngpoint2  = mMap.getProjection().fromScreenLocation(p);
						    points.add(latlngpoint2);
						    
						    //Canvas.drawLine(start_x, start_y, end_x, end_y, paint_obj);
						    canvas.drawLine(endX, endY, startX, startY, paint);
						    drawable.invalidate();
						    
						    // latitudine
						    if(latlngpoint2.latitude > maxnord) maxnord = latlngpoint2.latitude;
						    if(latlngpoint2.latitude < maxsud) maxsud = latlngpoint2.latitude;
					    	
					    	// longitudine
						    if(latlngpoint2.longitude > maxest) maxest = latlngpoint2.longitude;
						    if(latlngpoint2.longitude < maxovest) maxovest = latlngpoint2.longitude;
						    
						    // aggiungi al poligono virtuale
					    	android_polygon_builder.addVertex(new it.oncreate.testmaps.sromku.Point(endX, endY));
					    	
					    	// genera poligono virtuale
					    	android_polygon = android_polygon_builder.build();
						    
						    Toast.makeText(MainActivity.this, "MaxLatNord: "+maxnord+"\n"+"MaxLatSud: "+maxsud+"\n"+"MaxLngOvest: "+maxovest+"\n"+"MaxLngEst: "+maxest, Toast.LENGTH_LONG).show();
						    
						    // disegna poligono sulla mappa
						    PolygonOptions options = new PolygonOptions();
						    options.strokeColor(Color.DKGRAY).fillColor(Color.argb(60, 0, 0, 0));
						    
						    int i;
						    for(i = 0; i < points.size(); i++){
						    	options.add(points.get(i));
						    }
						    if(i > 3){ // almeno tre punti per disegnare un poligono
						    	Polygon poligono_su_mappa = mMap.addPolygon(options);
							    poligoni_su_mappa.add(poligono_su_mappa);
							    
							    // SCARICA DATI AREA SELEZIONATA
							    // es.: in caso di chiamata verso un web-service è possibile inviare le
							    // coordinate di massima/minima latitudine e massima/minima longitudine;
							    // i risultati saranno, in seguito, ulteriormente filtrati per mezzo del metodo
							    // android_polygon.contains(pnt)
							    
							    String data = getJSONData();
							    if(data != null){
							    	try {
										JSONArray luoghi = new JSONArray(data);
										for(int x = 0; x < luoghi.length(); x++){
											JSONObject coordinate = luoghi.getJSONObject(x);
											Float lat = Float.parseFloat(coordinate.getString("lat"));
											Float lng = Float.parseFloat(coordinate.getString("lng"));
											LatLng c = new LatLng(lat, lng);
											it.oncreate.testmaps.sromku.Point pnt = getPointFromCoordinates(c);
											if(android_polygon.contains(pnt)){
												Marker mk = mMap.addMarker(new MarkerOptions().position(c).title("data"));
												marker_su_mappa.add(mk);
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
							    }
							    else{
							    	Toast.makeText(MainActivity.this, "Data file not found!", Toast.LENGTH_SHORT).show();
							    }
						    }
						    else{
						    	Toast.makeText(MainActivity.this, "Sono necessari almeno 3 punti per disegnare un poligono!", Toast.LENGTH_LONG).show();
						    }
						    
						    // svuota variabili
						    android_polygon_builder = null;
						    android_polygon = null;
					        points.clear();
						} catch (Exception e) {
							Toast.makeText(MainActivity.this, "Area non definita!", Toast.LENGTH_SHORT).show();
						}
					    
					    return true;
					case MotionEvent.ACTION_CANCEL:
					    break;
					default:
					    break;
				}

				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	        
	        // disabilita il ZoomControl
	        UiSettings setting = mMap.getUiSettings();
	        setting.setZoomControlsEnabled(false);
	        
	        // Check if we were successful in obtaining the map.
	        if (mMap != null) {
	            // Add Marker
	        	mMap.addMarker(new MarkerOptions().position(ANCONA).title("Ancona"));
	        	
	        	// Move the camera instantly to Ancona with a zoom of 14.
	        	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ANCONA, 14));

	            // Zoom in, animating the camera.
	        	mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
	        }
	    }
	}
	
	private it.oncreate.testmaps.sromku.Point getPointFromCoordinates(LatLng c){
		android.graphics.Point p = mMap.getProjection().toScreenLocation(c);
		return new it.oncreate.testmaps.sromku.Point(p.x, p.y);
	}
	
	private String getJSONData(){
		InputStream is = getResources().openRawResource(R.raw.data);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
		    Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		    int n;
		    while ((n = reader.read(buffer)) != -1) {
		        writer.write(buffer, 0, n);
		    }
		} catch (UnsupportedEncodingException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
		    try {
				is.close();
			} catch (IOException e) {
				is = null;
			}
		}
		return writer.toString();
	}
	
}
