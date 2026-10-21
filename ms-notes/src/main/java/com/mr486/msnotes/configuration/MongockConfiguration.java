package com.mr486.msnotes.configuration;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.context.annotation.Configuration;

/**
 * Active Mongock, qui applique au démarrage les migrations du package {@code dbchangelogs}.
 *
 * <p>L'activation vit dans sa propre classe plutôt que sur {@code MsNotesApplication} : les tests
 * de tranche ({@code @WebMvcTest}, {@code @DataMongoTest}) ignorent les classes de configuration
 * de l'application, alors qu'ils héritent des annotations de la classe principale. Placé là,
 * Mongock exigerait une base complète jusque dans les tests du seul contrôleur.</p>
 *
 * <p><b>Exemple :</b> avec {@code MONGOCK_PROFILES=demo}, le premier démarrage insère les 9 notes
 * de démonstration ; les suivants ne rejouent rien, Mongock ayant consigné la migration.</p>
 */
@Configuration
@EnableMongock
public class MongockConfiguration {
}
