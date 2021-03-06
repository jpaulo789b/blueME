package net.icfatesg.blueme.services;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.icfatesg.blueme.model.Evento;
import net.icfatesg.blueme.model.Oficina;
import net.icfatesg.blueme.model.OficinaVisitada;
import net.icfatesg.blueme.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by minerthal on 12/06/17.
 */

public class FireBase {
    private DatabaseReference mEvento;
    private DatabaseReference mUsuario;
    private DatabaseReference mOficinas;
    private DatabaseReference mOficinasVisitadas;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public FireBase() {
        this.mAuth =  FirebaseAuth.getInstance();
        this.currentUser = mAuth.getCurrentUser();
        this.mEvento = FirebaseDatabase.getInstance().getReference().child("Evento");
        this.mUsuario = FirebaseDatabase.getInstance().getReference().child("Usuarios").child(currentUser.getUid());
        this.mOficinasVisitadas = mUsuario;
        this.mOficinas = FirebaseDatabase.getInstance().getReference().child("Oficinas");

    }

    public void salvarOfic(OficinaVisitada oficinaVisitada){
        this.mOficinasVisitadas
                .child(oficinaVisitada.getIDEVENTO())
                .child(oficinaVisitada.getIDOFICINA())
                .setValue(oficinaVisitada);
    }


    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public void updateUsuario(Usuario usuario){
        this.mUsuario.setValue(usuario);
    }
    public void inserirUsuario(Usuario usuario){
        this.mUsuario.child(mUsuario.push().getKey()).setValue(usuario);
    }

    private void keepOnThePhone(){
        this.mEvento.keepSynced(true);
        this.mUsuario.keepSynced(true);
    }

    public void getOficinas(final CallbackOficinas callback,Evento evento){
        this.mOficinas.child(evento.getID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Oficina> oficinas = new ArrayList<Oficina>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    oficinas.add(child.getValue(Oficina.class));
                }
                callback.getOficinas(oficinas);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void getOfificasVisitadas(final CallbackOficinasVisitadas callback,String eventoID){
        this.mOficinasVisitadas.child(eventoID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<OficinaVisitada> oficinas = new ArrayList<OficinaVisitada>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    for (DataSnapshot childOf:child.getChildren()){
                        OficinaVisitada of = childOf.getValue(OficinaVisitada.class);
                        of.setIDEVENTO(child.getKey());
                        oficinas.add(of);
                    }
                }
                callback.getOficinasVisitadas(oficinas);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getEventos(final CallbackEventos callback){
        this.mEvento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Evento> objects = new ArrayList<Evento>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    objects.add(child.getValue(Evento.class));
                }
                callback.getEventos(objects);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void getUsuario(final CallbackUsuario callback){
        this.mUsuario.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Usuario u = new Usuario();
                    dataSnapshot.getValue();
                    u.setEmail(dataSnapshot.getValue(Usuario.class).getEmail());
                    u.setNome(dataSnapshot.getValue(Usuario.class).getNome());
                    u.setSenha(dataSnapshot.getValue(Usuario.class).getSenha());
                    u.setBluetoothMAC(dataSnapshot.getValue(Usuario.class).getBluetoothMAC());
                    u.setID(dataSnapshot.getValue(Usuario.class).getID());
                    callback.getUsuario(u);
                }catch (Exception e){
                    Log.d("fatal",e.getMessage());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public interface CallbackOficinas{
        void getOficinas(List<Oficina> oficinaList);
    }
    public interface CallbackOficinasVisitadas{
        void getOficinasVisitadas(List<OficinaVisitada> oficinaVisitadaList);
    }
    public interface CallbackEventos{
        void getEventos(List<Evento> eventoList );
    }
    public interface CallbackUsuario{
        void getUsuario(Usuario usuario);
    }

}
