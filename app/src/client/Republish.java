package client;

import data.Message;
import data.User;
import data.UserDB;
import server.Parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Scanner;

import static client.Client.OK;

public class Republish implements ClientAction{

    String username;
    ByteBuffer buffer;
    SocketChannel client;



    public Republish(String username, ByteBuffer buffer, SocketChannel client) {
        this.username = username;
        this.buffer = buffer;
        this.client= client;
    }


    @Override
    public String getCommand() {
        System.out.println("Which message you want to republish ? We need the id");
        Scanner scanner = new Scanner(System.in);
        long id = scanner.nextLong();
        return "REPUBLISH author:" + username + " msg_id: " + id;
    }

    @Override
    public void run() throws IOException, InterruptedException {
        while(true){
            String message = this.getCommand();
            buffer = ByteBuffer.wrap(message.getBytes());
            client.write(buffer);
            buffer.clear();

            /*** receive message ***/
            client.read(buffer);
            String response = new String(buffer.array(), 0, buffer.position());
            buffer.flip();
            buffer.clear();

            System.out.println(response);

            /*** Close connexion ***/
            if (response.contains(OK)) {
                break;
            }
        }
        /*
        String id = Parser.parserRepublish(command).get(3);
        StringBuilder msg = new StringBuilder();
        List <Long> messages =  userDB.getMessages(username);
        for (int i=0 ; i< messages.size(); i++){
            if(messages.get(i).equals(id))
                msg.append(messages.get(i));
        }*/
    }
    // verif client
    // check si c'est bien un author
    // --mettre à jour le package data pour recup les données des clients
    // modfi client pour recup les bonnes requetes
    // faire la bonne rep niveau server

    /*
     * commande rcdvids de l'author
     * selectionné l'id
     * dans le client, on lance un follower sur soit même
     *
     */
}
