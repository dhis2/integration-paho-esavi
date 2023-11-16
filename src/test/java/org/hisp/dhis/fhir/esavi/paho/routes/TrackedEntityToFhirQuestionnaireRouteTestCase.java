package org.hisp.dhis.fhir.esavi.paho.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.hisp.dhis.api.model.v2_38_1.Attribute__2;
import org.hisp.dhis.api.model.v2_38_1.DataValue__3;
import org.hisp.dhis.api.model.v2_38_1.DescriptiveWebMessage;
import org.hisp.dhis.api.model.v2_38_1.EnrollmentStatus;
import org.hisp.dhis.api.model.v2_38_1.Enrollment__2;
import org.hisp.dhis.api.model.v2_38_1.EventChart;
import org.hisp.dhis.api.model.v2_38_1.Event__2;
import org.hisp.dhis.api.model.v2_38_1.OrganisationUnit;
import org.hisp.dhis.api.model.v2_38_1.OrganisationUnitLevel;
import org.hisp.dhis.api.model.v2_38_1.TrackedEntity;
import org.hisp.dhis.api.model.v2_38_1.WebMessage;
import org.hisp.dhis.integration.esavi.Application;
import org.hisp.dhis.integration.sdk.Dhis2ClientBuilder;
import org.hisp.dhis.integration.sdk.api.Dhis2Client;
import org.hisp.dhis.integration.sdk.internal.security.BasicCredentialsSecurityContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class )
@CamelSpringBootTest
@UseAdviceWith
public class TrackedEntityToFhirQuestionnaireRouteTestCase
{
    @Container
    public static PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    @Container
    public static GenericContainer<?> DHIS2_CONTAINER;

    @Value( "${dhis2-to-esavi.dhis2.esavi-program-stage-id}" )
    protected String esaviProgramStageId;

    @LocalServerPort
    protected int serverPort;

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    protected ProducerTemplate producerTemplate;

    private static final String ADMIN_USER_ID = "M5zQapPyTZI";

    private static boolean doBeforeEach = true;

    private static String dhis2Url;

    private String trackedEntityId;

    private static PostgreSQLContainer<?> newPostgreSqlContainer()
    {
        return new PostgreSQLContainer<>( DockerImageName.parse( "postgis/postgis:12-3.3-alpine" )
            .asCompatibleSubstituteFor( "postgres" ) )
            .withDatabaseName( "dhis2" )
            .withNetworkAliases( "db" )
            .withUsername( "dhis" )
            .withStartupTimeout( Duration.of( 5, ChronoUnit.MINUTES ) )
            .withPassword( "dhis" ).withNetwork( Network.newNetwork() );
    }

    private static GenericContainer<?> newDhis2Container( PostgreSQLContainer<?> postgreSqlContainer )
    {
        return new GenericContainer<>( DockerImageName.parse( "dhis2/core:2.38.4.3" ) )
            .dependsOn( postgreSqlContainer )
            .withClasspathResourceMapping( "dhis.conf", "/opt/dhis2/dhis.conf", BindMode.READ_WRITE )
            .withNetwork( postgreSqlContainer.getNetwork() ).withExposedPorts( 8080 )
            .waitingFor( new HttpWaitStrategy().forStatusCode( 200 ).withStartupTimeout( Duration.ofSeconds( 120 ) ) )
            .withEnv( "WAIT_FOR_DB_CONTAINER", "db" + ":" + 5432 + " -t 0" );
    }

    @BeforeAll
    public static void beforeAll()
    {
        POSTGRESQL_CONTAINER = newPostgreSqlContainer();
        POSTGRESQL_CONTAINER.start();
        DHIS2_CONTAINER = newDhis2Container( POSTGRESQL_CONTAINER );
        DHIS2_CONTAINER.start();

        dhis2Url = String.format( "http://localhost:%s/api", DHIS2_CONTAINER.getFirstMappedPort() );

        System.setProperty( "dhis2-to-esavi.dhis2.base-url", dhis2Url );
        System.setProperty( "dhis2-to-esavi.dhis2.username", "admin" );
        System.setProperty( "dhis2-to-esavi.dhis2.password", "district" );
    }

    @BeforeEach
    public void beforeEach()
        throws
        Exception
    {
        if ( doBeforeEach )
        {
            Dhis2Client dhis2Client = Dhis2ClientBuilder.newClient( dhis2Url,
                    new BasicCredentialsSecurityContext( "admin", "district" ), 5, 300000, 0, 50000, 50000, 50000 )
                .build();

            String orgUnitId = createOrgUnit( dhis2Client );

            createOrgUnitLevel( dhis2Client );
            addOrgUnitToUser( orgUnitId, ADMIN_USER_ID, dhis2Client );
            importMetaData( orgUnitId, dhis2Client );
            trackedEntityId = createEnrollment( orgUnitId, dhis2Client );

            doBeforeEach = false;
        }
    }

    private String createEnrollment( String orgUnitId, Dhis2Client dhis2Client )
        throws
        ParseException
    {
        TrackedEntity tei = new TrackedEntity().withAttributes(
                List.of( new Attribute__2().withAttribute( "KSr2yTdu1AI" ).withValue( "DEM_2023_11_09_000002" ),
                    new Attribute__2().withAttribute( "oindugucx72" ).withValue( "1" ) ) )
            .withEnrollments( List.of(
                new Enrollment__2().withEnrolledAt( new SimpleDateFormat( "yyyy-MM-dd" ).parse( "2022-01-19" ) )
                    .withProgram( "aFGRl00bzio" ).withOrgUnit( orgUnitId )
                    .withStatus( EnrollmentStatus.ACTIVE ).withEvents(
                        List.of( new Event__2().withStatus( EventChart.EventStatus.COMPLETED )
                            .withOccurredAt( "2023-11-13T16:04:26.573" )
                            .withProgramStage( esaviProgramStageId )
                            .withProgram( "aFGRl00bzio" ).withOrgUnit( orgUnitId ).withDataValues( List.of(
                                new DataValue__3().withDataElement( "PW0dQpcY2wD" ).withValue( "2023-11-09" )
                                    .withProvidedElsewhere( false ) ) ) ) ) ) )
            .withOrgUnit( orgUnitId )
            .withTrackedEntityType( "bip5wHrcB0G" );

        WebMessage webMessage = dhis2Client.post( "tracker" )
            .withResource( Map.of( "trackedEntities", List.of( tei ) ) ).withParameter( "async", "false" )
            .transfer().returnAs( WebMessage.class );

        if ( !webMessage.getStatus().get().equals( DescriptiveWebMessage.Status.OK ) )
        {
            throw new RuntimeException();
        }

        return (String) ((List<Map<String, Object>>) ((Map<String, Map<String, Object>>) ((Map<String, Object>) webMessage.getAdditionalProperties()
            .get( "bundleReport" )).get( "typeReportMap" )).get( "TRACKED_ENTITY" ).get( "objectReports" )).get( 0 )
            .get( "uid" );
    }

    private void addOrgUnitToUser( String orgUnitId, String userId, Dhis2Client dhis2Client )
        throws
        IOException
    {
        dhis2Client.post( "/users/{userId}/organisationUnits/{organisationUnitId}", userId, orgUnitId ).transfer()
            .close();
    }

    private void createOrgUnitLevel( Dhis2Client dhis2Client )
        throws
        IOException
    {
        dhis2Client.post( "filledOrganisationUnitLevels" )
            .withResource( new OrganisationUnitLevel().withName( "Level 1" ).withLevel( 1 ) ).transfer().close();
    }

    private String createOrgUnit( Dhis2Client dhis2Client )
    {
        return (String) ((Map) dhis2Client.post( "organisationUnits" )
            .withResource(
                new OrganisationUnit().withName( "Acme" ).withShortName( "Acme" ).withOpeningDate( new Date() ) )
            .transfer().returnAs(
                WebMessage.class ).getResponse().get()).get( "uid" );
    }

    private void importMetaData( String orgUnitId, Dhis2Client dhis2Client )
        throws
        IOException
    {
        String metaData = new String(
            Thread.currentThread().getContextClassLoader()
                .getResourceAsStream( "metadata.json" )
                .readAllBytes(),
            Charset.defaultCharset() );

        dhis2Client.post( "metadata" ).withResource( metaData.replaceAll( "<OU_UID>", orgUnitId ) )
            .withParameter( "atomicMode", "NONE" ).transfer()
            .close();
    }

    @Test
    public void testQuestionnaireResponse()
        throws
        Exception
    {
        camelContext.start();
        String replyBody = producerTemplate.requestBody(
            String.format( "http://0.0.0.0:%s/fhir/baseR4/QuestionnaireResponse/%s", serverPort, trackedEntityId ),
            null, String.class );

        assertThatJson( replyBody ).isEqualTo(
            new String(
                Thread.currentThread().getContextClassLoader().getResourceAsStream( "expected-fhir-payload.json" )
                    .readAllBytes(), Charset.defaultCharset() ).replace( "<TRACKED_ENTITY_ID>", trackedEntityId ) );

        assertThatJson( Files.readString( Paths.get( "output/fhir-payload.json" ) ) ).isEqualTo(
            new String(
                Thread.currentThread().getContextClassLoader().getResourceAsStream( "expected-fhir-payload.json" )
                    .readAllBytes(), Charset.defaultCharset() ).replace( "<TRACKED_ENTITY_ID>", trackedEntityId ) );
    }
}
