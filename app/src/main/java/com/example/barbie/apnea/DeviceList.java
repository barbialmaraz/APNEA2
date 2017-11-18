package com.example.barbie.apnea;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceList extends AppCompatActivity {


    private Button btnScan;
    private ListView listViewDeDispositivos;
    private BluetoothAdapter myBluetooth = null;
    private Switch switch_bluetooth;
    public static String EXTRA_ADDRESS = "device_address";
    private TextView text_no_items;
    private TextView text_titulo;
    private ProgressBar progressBarCircular;
    private Set<BluetoothDevice> arrayDeDispositivos;
    private ArrayList<BluetoothDevice> listaDeDispEncontrados = new ArrayList<BluetoothDevice>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Se genera solo cuando se crea la activity...
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btdevice_list);

        //Para añadir el boton con la flecha para ir a la activity anterior
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        //Se inicializa el manejador del adaptador bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();





        //Se definen los componentes del layout
        btnScan = (Button)findViewById(R.id.btn_scan);
        listViewDeDispositivos = (ListView)findViewById(R.id.listView);
        switch_bluetooth = (Switch)findViewById(R.id.switch1);
        text_no_items = (TextView)findViewById(R.id.text_no_items);
        text_titulo = (TextView)findViewById(R.id.textView);
        progressBarCircular = (ProgressBar)findViewById(R.id.progressBar);

        progressBarCircular.setVisibility(View.INVISIBLE);

        mostrarComponentes();

        //Se intenta listar los dispositivos conectados, siempre y cuando el bluetooth este encendido...


        //Listener con el metodo del Switch en caso de que este "ON" o "OFF"
        switch_bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true){
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, 1);
                }else{
                    myBluetooth.disable();
                    bluetoothDesactivado();

                }
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myBluetooth.startDiscovery();
            }
        });


        //se definen un broadcastReceiver que captura el broadcast del SO cuando captura los siguientes eventos:
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //Cambia el estado del Bluethoot (Acrtivado /Desactivado)
        filter.addAction(BluetoothDevice.ACTION_FOUND); //Se encuentra un dispositivo bluethoot al realizar una busqueda
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //Cuando se comienza una busqueda de bluethoot
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //cuando la busqueda de bluethoot finaliza

        //se define (registra) el handler que captura los broadcast anterirmente mencionados.
        registerReceiver(mReceiver, filter);


    }

    @Override
    //Cuando se detruye la Acivity se quita el registro de los brodcast. Apartir de este momento no se
    //recibe mas broadcast del SO. del bluethoot
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        mostrarComponentes();
    }

    //En la ventana de confirmacion para encender el bluetooth se elige "Aceptar" o "Cancelar"
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode == RESULT_CANCELED)
                {
                    bluetoothDesactivado();
                }else{
                    bluetoothActivado();
                }


    }




    private void listarDispositivosvinculados()
    {
        arrayDeDispositivos = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (arrayDeDispositivos.size()>0)
        {
            for(BluetoothDevice bt : arrayDeDispositivos)
            {
                //if(bt.getName().contains("Philips"))
                    list.add(bt.getName() + "\n" + bt.getAddress()); //Obtenemos los nombres y direcciones MAC de los disp. vinculados
            }
        }
        if(list.isEmpty()) {
            listViewDeDispositivos.setVisibility(View.INVISIBLE);
            text_no_items.setText("No se han encontrado dispositivos vinculados");
            text_no_items.setVisibility(View.VISIBLE);
        }
        else {
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
            listViewDeDispositivos.setAdapter(adapter);
            text_no_items.setVisibility(View.INVISIBLE);
            listViewDeDispositivos.setVisibility(View.VISIBLE);
            //listViewDeDispositivos.setOnItemClickListener(myListClickListener);
        }
    }



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            //Atraves del Intent obtengo el evento de Bluethoot que informo el broadcast del SO
            String action = intent.getAction();

            //Si cambio de estado el Bluethoot(Activado/desactivado)
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                //Obtengo el parametro, aplicando un Bundle, que me indica el estado del Bluethoot
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                //Si esta activado
                if (state == BluetoothAdapter.STATE_ON)
                {
                    bluetoothActivado();
                }else{
                    bluetoothDesactivado();
                }
            }
            //Si se inicio la busqueda de dispositivos bluethoot
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                //Creo la lista donde voy a mostrar los dispositivos encontrados
                listaDeDispEncontrados = new ArrayList<BluetoothDevice>();

                //muestro el cuadro de dialogo de busqueda
                  estadoCargando();
            }
            //Si finalizo la busqueda de dispositivos bluethoot
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                //se cierra el cuadro de dialogo de busqueda
                //se inicia el activity DeviceListActivity pasandole como parametros, por intent,
                //el listado de dispositovos encontrados
                Intent newIntent = new Intent(DeviceList.this, FoundDeviceList.class);

                newIntent.putParcelableArrayListExtra("dispositivosEncontrados", listaDeDispEncontrados);

                startActivity(newIntent);


            }
            //si se encontro un dispositivo bluethoot
            else if (BluetoothDevice.ACTION_FOUND.equals(action))
            {

                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                listaDeDispEncontrados.add(device);
                Toast.makeText(DeviceList.this,"Dispositivo Encontrado:" + device.getName(),Toast.LENGTH_SHORT);


            }
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here

                Intent intent = new Intent(DeviceList.this, Inicio.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }





    public void bluetoothActivado(){
        switch_bluetooth.setChecked(true);
        listarDispositivosvinculados();
    }

    public void bluetoothDesactivado(){
        switch_bluetooth.setChecked(false);
        listViewDeDispositivos.setVisibility(View.INVISIBLE);
        text_no_items.setText("Encienda el Bluetooth para ver los dispositivos APNEA conectados");
        text_no_items.setVisibility(View.VISIBLE);
    }

    public void estadoCargando(){
        getSupportActionBar().setTitle("Buscando...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        listViewDeDispositivos.setVisibility(View.INVISIBLE);
        btnScan.setVisibility(View.INVISIBLE);
        text_no_items.setVisibility(View.INVISIBLE);
        text_titulo.setVisibility(View.INVISIBLE);
        switch_bluetooth.setVisibility(View.INVISIBLE);
        progressBarCircular.setVisibility(View.VISIBLE);
    }

    public void mostrarComponentes(){
        getSupportActionBar().setTitle("Conexión");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if(myBluetooth.isEnabled()) {
            bluetoothActivado();
        }else{
            bluetoothDesactivado();
        }
        if (myBluetooth == null)
        {
            //si el celular no soporta bluethoot
            text_no_items.setText("El Bluetooth no es soportado por este dispositivo movil.");
            text_no_items.setTextColor(Color.RED);
            switch_bluetooth.setEnabled(false);
        }

        btnScan.setVisibility(View.VISIBLE);
        text_titulo.setVisibility(View.VISIBLE);
        switch_bluetooth.setVisibility(View.VISIBLE);
        progressBarCircular.setVisibility(View.INVISIBLE);
    }



}
