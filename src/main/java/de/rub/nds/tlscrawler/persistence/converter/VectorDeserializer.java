/*
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rub.nds.tlscrawler.persistence.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.rub.nds.tlsattacker.attacks.general.Vector;
import java.io.IOException;

public class VectorDeserializer extends StdDeserializer<Vector> {
    
    public VectorDeserializer() {
        super(Vector.class);
    }

    @Override
    public Vector deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        return null;
    }

}

