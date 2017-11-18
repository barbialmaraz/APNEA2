package com.example.barbie.apnea;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class FoundDeviceList extends AppCompatActivity {

    private ListView listViewDisp;
    private TextView text_no_items;
    private TextView text_titulo;
    private ArrayList<BluetoothDevice> arrayListDisp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_device_list);
        //Para añadir el boton con la flecha para ir a la activity anterior
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Dispositivos encontrados");



        listViewDisp = (ListView)findViewById(R.id.listView);
        text_no_items = (TextView)findViewById(R.id.text_no_disp);
        text_titulo = (TextView)findViewById(R.id.text_title);

        Intent intent = getIntent();

        arrayListDisp = intent.getParcelableArrayListExtra("dispositivosEncontrados");

        ArrayList list = new ArrayList();

        if(arrayListDisp.isEmpty()){
            noHayDispositivos();
        }else {
            for (BluetoothDevice bt : arrayListDisp) {
                //if(bt.getName().contains("Philips"))
                list.add(bt.getName() + "\n" + bt.getAddress()); //Obtenemos los nombres y direcciones MAC de los disp. vinculados
            }

            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
            listViewDisp.setAdapter(adapter);
            hayDispositivos();
        }




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here

                Intent intent = new Intent(FoundDeviceList.this, DeviceList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void hayDispositivos(){
        text_no_items.setVisibility(View.INVISIBLE);
        listViewDisp.setVisibility(View.VISIBLE);
        text_titulo.setVisibility(View.VISIBLE);
    }

    public void noHayDispositivos(){
        text_no_items.setVisibility(View.VISIBLE);
        listViewDisp.setVisibility(View.INVISIBLE);
        text_titulo.setVisibility(View.INVISIBLE);
    }

}
