/*
Copyright 2023 Fausto Spoto

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.hotmoka.websockets.client;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.glassfish.tyrus.client.ClientManager;

import io.hotmoka.websockets.client.api.ClientEndpoint;
import io.hotmoka.websockets.client.api.WebSocketClient;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.Decoder;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.Endpoint;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

/**
 * Partial implementation of a websocket client endpoint.
 *
 * @param <C> the type of the client this endpoint works for
 */
public abstract class AbstractClientEndpoint<C extends WebSocketClient> extends Endpoint implements ClientEndpoint<C> {

	/**
	 * Deploys this endpoint at the given URI, with the given input message type and the
	 * given output message type.
	 * 
	 * @param uri the URI
	 * @param input the input message type
	 * @param output the output message type
	 * @return the resulting session
	 * @throws DeploymentException if the endpoint cannot be deployed
	 * @throws IOException if an I/O error occurs
	 */
	protected Session deployAt(URI uri, Class<? extends Decoder> input, Class<? extends Encoder> output) throws DeploymentException, IOException {
		var config = ClientEndpointConfig.Builder.create()
			.decoders(List.of(input))
			.encoders(List.of(output))
			.build();

		return ClientManager.createClient().connectToServer(this, config, uri);
	}

	/**
	 * Deploys this endpoint at the given URI, with the given input message types and the
	 * given output message type.
	 * 
	 * @param uri the URI
	 * @param input1 the first input message type
	 * @param input2 the second input message type
	 * @param output the output message type
	 * @return the resulting session
	 * @throws DeploymentException if the endpoint cannot be deployed
	 * @throws IOException if an I/O error occurs
	 */
	protected Session deployAt(URI uri, Class<? extends Decoder> input1, Class<? extends Decoder> input2, Class<? extends Encoder> output) throws DeploymentException, IOException {
		var config = ClientEndpointConfig.Builder.create()
			.decoders(List.of(input1, input2))
			.encoders(List.of(output))
			.build();

		return ClientManager.createClient().connectToServer(this, config, uri);
	}

	/**
	 * Adds the given handler for incoming messages to the given session.
	 * 
	 * @param <M> the type of the messages
	 * @param session the session
	 * @param handler the handler
	 */
	protected <M> void addMessageHandler(Session session, Consumer<M> handler) {
		session.addMessageHandler((MessageHandler.Whole<M>) handler::accept);
	}

	/**
	 * Sends the given object, synchronously, with the given session.
	 * 
	 * @param session the session
	 * @param object the object to send
	 * @throws IOException if an IOException occurs
	 * @throws EncodeException if there was a problem encoding the message object
	 */
	protected void sendObject(Session session, Object object) throws IOException, EncodeException {
		session.getBasicRemote().sendObject(object);
	}

	/**
	 * Sends the given object, asynchronously, with the given session.
	 * 
	 * @param session the session
	 * @param object the object to send
	 * @return the future that can be used to wait for the operation to complete
	 */
	protected Future<Void> sendObjectAsync(Session session, Object object) {
		return session.getAsyncRemote().sendObject(object);
	}
}