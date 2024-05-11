package de.eldecker.dhbw.spring.glossar.web;

import static java.time.LocalDateTime.now;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.eldecker.dhbw.spring.glossar.db.Datenbank;
import de.eldecker.dhbw.spring.glossar.db.entities.GlossarEntity;


/**
 * REST-Controller-Klasse zur Bereitstellung der REST-Endpunkte,
 * die vom Frontend (HTML+JavaScript im Browser) angesprochen werden.
 */
@RestController
@RequestMapping( "/api/v1" )
public class RestApiController {

    private static Logger LOG = LoggerFactory.getLogger( RestApiController.class );

    /** Zielklasse für Deserialisierung der über HTTP-POST-Request empfangenen Payload. */
    public record Payload( long id, String begriff, String erklaerung ) { }

    /** Repository-Bean für Zugriff auf Datenbank. */
    private final Datenbank _datenbank;

    /** Bean für Deserialisierung von JSON-Playload. */
    private final ObjectMapper _objectMapper;


    /**
     * Konstruktor für <i>Dependency Injection</i>.
     */
    @Autowired
    public RestApiController( Datenbank datenbank,
                              ObjectMapper objectMapper ) {

        _datenbank    = datenbank;
        _objectMapper = objectMapper;
    }


    /**
     * Endpunkt für HTTP-POST-Request zum Anlegen neuer Eintrag.
     *
     * @param jsonPayload  JSON-Payload mit neuem Begriff und Erklärung.
     *
     * @param authentication Objekt für Abfrage authentifizierter Nutzer.
     *
     * @return HTTP-Status 201 wenn erfolgreich, 401 wenn keine Berechtigung,
     *         400 bei ungültiger JSON-Payload, 409 bei Konflikt (Eintrag bereits
     *         vorhanden).
     */
    @PostMapping( "/neu" )
    @Transactional
    public ResponseEntity<String> eintragNeuAendern( @RequestBody String jsonPayload,
                                                     Authentication authentication ) {

        if ( authentication == null || authentication.isAuthenticated() == false ) {

            LOG.warn( "Versuch neuen Eintrag anzulegen, aber Nutzer ist nicht authentifziert." );
            return new ResponseEntity<>( "Keine Berechtigung einen neuen Eintrag anzulegen", UNAUTHORIZED ); // HTTP-Status-Code 401
        }

        LOG.info( "JSON-Payload für neuen Glossareintrag über HTTP-POST erhalten: {}", jsonPayload );

        Payload payloadObjekt = null;
        try {

            payloadObjekt = _objectMapper.readValue( jsonPayload, Payload.class );
        }
        catch ( JsonProcessingException ex ) {

            LOG.error( "Fehler bei Deserialisierung von HTTP-Payload mit neuem Eintrag.", ex );
            return new ResponseEntity<>( "Ungültige JSON-Payload.", BAD_REQUEST);
        }

        final String begriffNeu = payloadObjekt.begriff();
        final Optional<GlossarEntity> eintragAlt = _datenbank.getEintragByBegriff( begriffNeu );
        if ( eintragAlt.isPresent() ) {

            LOG.warn( "Glossareintrag mit Begriff \"{}\" bereits vorhanden.", begriffNeu );
            return new ResponseEntity<>( "Eintrag mit Begriff bereits vorhanden.", CONFLICT );
        }

        final LocalDateTime jetzt = now();

        final GlossarEntity eintragNeu = new GlossarEntity( begriffNeu,
                                                            payloadObjekt.erklaerung(),
                                                            jetzt, jetzt );

        final long idNeu = _datenbank.neuerGlossarEintrag( eintragNeu );
        LOG.info( "Neuer Glossareintrag mit ID {} angelegt.", idNeu );

        return new ResponseEntity<>( "Neuer Eintrag im Glossar gespeichert", CREATED ); // HTTP-Status-Code 201
    }
}
