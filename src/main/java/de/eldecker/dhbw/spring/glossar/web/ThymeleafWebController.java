package de.eldecker.dhbw.spring.glossar.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import de.eldecker.dhbw.spring.glossar.db.Datenbank;
import de.eldecker.dhbw.spring.glossar.db.entities.GlossarEntity;


/**
 * Controller (kein RestController!), der die Anfragen für die Thymeleaf-Views bearbeitet.
 * Alle Pfade beginnen mit {@code /app/}.
 */
@Controller
@RequestMapping( "/app" )
public class ThymeleafWebController {

    private static final Logger LOG = LoggerFactory.getLogger( ThymeleafWebController.class );
        
    /** Attribut-Key für Template "hauptseite"; referenziert leeren String, wenn kein Nutzer angemeldet. */
    private static final String ATTRIBUT_NUTZER = "nutzername";
    
    /** Attribut-Key für Template "hauptseite" mit Liste der Einträge. */
    private static final String ATTRIBUT_BEGRIFF_LISTE = "begriffe";
    
    /** Repository-Bean für Zugriff auf Datenbank. */
    private final Datenbank _datenbank;
    
    
    /**
     * Konstruktor für <i>Dependency Injection</i>.
     */    
    @Autowired
    public ThymeleafWebController( Datenbank datenbank ) {
        
        _datenbank = datenbank;
    }
    
    
    /**
     * Übersichtsseite mit Liste der Glossareinträge (aber ohne Erklärungen) anzeigen. 
     * 
     * @param authentication Objekt zur Abfrage, ob Nutzer authentifiziert ist;
     *                       ACHTUNG: ist {@code null} für unangemeldete Nutzer.
     * 
     * @param model Objekt, in das die Werte für die Platzhalter in der Template-Datei
     *              geschrieben werden. 
     * 
     * @return Name (ohne Suffix) der Template-Datei {@code hauptseite.html}, die angezeigt
     *         werden soll; wird in Ordner {@code src/main/resources/templates/} gesucht.
     */
    @GetMapping( "/hauptseite" )
    public String hauptseiteAnzeige( Authentication authentication,
                                     Model model ) {
                        
        final boolean nutzerIstAngemeldet = authentication != null && 
                                            authentication.isAuthenticated();        
        if ( nutzerIstAngemeldet ) {
            
            final String nutzername = authentication.getName();
            LOG.info( "Zugriff auf Hauptseite von Nutzer \"{}\".", nutzername );
            model.addAttribute( ATTRIBUT_NUTZER, nutzername );
            
        } else {
        
            LOG.info( "Zugriff auf Hauptseite von unangemeldetem Nutzer." );
            model.addAttribute( ATTRIBUT_NUTZER, "" );
        }
                
        final List<GlossarEntity> begriffListe = _datenbank.getGlossarBegriffe();
        model.addAttribute( ATTRIBUT_BEGRIFF_LISTE, begriffListe );        
                               
        return "hauptseite";
    }
    
}