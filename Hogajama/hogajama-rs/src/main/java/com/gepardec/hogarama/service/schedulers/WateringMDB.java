package com.gepardec.hogarama.service.schedulers;

import java.io.IOException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
// import org.jboss.ejb3.annotation.ResourceAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.hogarama.domain.metrics.Metrics;
import com.gepardec.hogarama.domain.sensor.SensorData;
import com.gepardec.hogarama.domain.sensor.SensorNormalizer;
import com.gepardec.hogarama.domain.watering.WateringService;

// @ResourceAdapter("activemq-ext")
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "habarama::watering"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
    }
)
public class WateringMDB implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(WateringMDB.class);

    @Inject
    WateringService wateringSvc;

    @Inject
    SensorNormalizer sensorNormalizer;

	public void onMessage(Message message) {
	    log.debug("Receive message of type " + message.getClass().getName());
	    BytesMessage msg = (BytesMessage) message;
		try {
		    byte[] b = new byte[(int) msg.getBodyLength()];
		    msg.readBytes(b);
 
		    ObjectMapper mapper = new ObjectMapper();
            SensorData sensorData = sensorNormalizer.normalize(mapper.readValue(b, SensorData.class));

            Metrics.sensorValues.labels(sensorData.getSensorName(), sensorData.getLocation()).set(sensorData.getValue());

            wateringSvc.water(sensorData);
            
		} catch (JMSException | IOException e) {
			throw new RuntimeException("Error handling sensor data!", e);
		}
	}
}
