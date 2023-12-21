/*
 * Copyright (c) 2004-2023, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.integration.esavi.routes;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;
import org.hisp.dhis.api.model.v2_38_1.OptionSet;
import org.hisp.dhis.api.model.v2_38_1.TrackedEntity;
import org.hisp.dhis.integration.esavi.converters.v1.EsaviContext;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DhisEsaviRoute extends RouteBuilder
{
    @Override
    public void configure()
        throws Exception
    {
        from( "timer:foo?repeatCount=1" )
            .routeId( "DHIS2-to-ESAVI-FHIR" )
            .to( "direct:fetch-drug-form" )
            .to( "direct:fetch-drug-route" )
            .to( "direct:fetch-diluent" )
            .to( "direct:fetch-whodrug" )
            .to( "direct:fetch-whodrug-vaccines" )
            .to( "direct:fetch-meddra" )
            .log( "Preload done." );

        from( "direct:fetch-drug-form" )
                .routeId( "Fetch-Drug-Form" )
                .setHeader( "CamelDhis2.queryParams", () -> Map.of(
                        "fields", "id,code,name,options[id,code,name]" ) )
                .to( "dhis2://get/resource?path=optionSets/qRyur64ZaPK&client=#dhis2Client" )
                .unmarshal().json( OptionSet.class )
                .process( ex -> EsaviContext.addOptionSet( ex.getIn().getBody( OptionSet.class ) ) );

        from( "direct:fetch-drug-route" )
                .routeId( "Fetch-Drug-Route" )
                .setHeader( "CamelDhis2.queryParams", () -> Map.of(
                        "fields", "id,code,name,options[id,code,name]" ) )
                .to( "dhis2://get/resource?path=optionSets/E9d1xL5jsTJ&client=#dhis2Client" )
                .unmarshal().json( OptionSet.class )
                .process( ex -> EsaviContext.addOptionSet( ex.getIn().getBody( OptionSet.class ) ) );

        from( "direct:fetch-diluent" )
                .routeId( "Fetch-Diluent" )
                .setHeader( "CamelDhis2.queryParams", () -> Map.of(
                        "fields", "id,code,name,options[id,code,name]" ) )
                .to( "dhis2://get/resource?path=optionSets/NdEeGMVaObK&client=#dhis2Client" )
                .unmarshal().json( OptionSet.class )
                .process( ex -> EsaviContext.addOptionSet( ex.getIn().getBody( OptionSet.class ) ) );

        from( "direct:fetch-whodrug" )
            .routeId( "Fetch-WHODrug" )
            .setHeader( "CamelDhis2.queryParams", () -> Map.of(
                    "fields", "id,code,name,options[id,code,name]" ) )
            .to( "dhis2://get/resource?path=optionSets/deNBd8tEIeD&client=#dhis2Client" )
            .unmarshal().json( OptionSet.class )
            .process( ex -> EsaviContext.addOptionSet( ex.getIn().getBody( OptionSet.class ) ) );

        from( "direct:fetch-whodrug-vaccines" )
            .routeId( "Fetch-WHODrug-Vaccines" )
            .setHeader( "CamelDhis2.queryParams", () -> Map.of(
                "fields", "id,code,name,options[id,code,name]" ) )
            .to( "dhis2://get/resource?path=optionSets/PrAA7nJPXke&client=#dhis2Client" )
            .unmarshal().json( OptionSet.class )
            .process( ex -> EsaviContext.addOptionSet( ex.getIn().getBody( OptionSet.class ) ) );

        from( "direct:fetch-meddra" )
            .routeId( "Fetch-MedDRA" )
            .setHeader( "CamelDhis2.queryParams", () -> Map.of(
                "fields", "id,code,name,options[id,code,name]" ) )
            .to( "dhis2://get/resource?path=optionSets/OzARj1D09Dm&client=#dhis2Client" )
            .unmarshal().json( OptionSet.class )
            .process( ex -> EsaviContext.addOptionSet( ex.getIn().getBody( OptionSet.class ) ) );

        rest( "/" )
            .get( "/QuestionnaireResponse/{trackedEntityId}" )
            .routeId( "get-esavi-cases" )
            .produces( MediaType.APPLICATION_JSON_VALUE )
            .to( "direct:fetch-esavi-cases" );

        from( "direct:fetch-esavi-cases" )
            .routeId( "Fetch-Esavi-Cases" )
            .setHeader( "CamelDhis2.queryParams")
                .groovy( "['program': 'aFGRl00bzio', 'ouMode': 'ACCESSIBLE', 'pageSize': '1', 'trackedEntity': request.headers.get('trackedEntityId'), 'fields': '*,enrollments[events[*],*]']" )
            .to( "dhis2://get/collection?path=tracker/trackedEntities&arrayName=instances&client=#dhis2Client" )
            .split(body()).aggregationStrategy( new UseLatestAggregationStrategy() )
                .wireTap( "direct:log-dhis2-payload" )
                .convertBodyTo( TrackedEntity.class )
                .convertBodyTo( Bundle.class )
                .to("direct:$validate")
                .marshal().fhirJson( "R4", true )
                .to("file://./output?fileName=QuestionnaireResponse.fhir.json&noop=true")
            .end();

        from( "direct:log-dhis2-payload" )
            .marshal( getJacksonDataFormat( Map.class, true ) )
            .to( "file://./output?fileName=TrackedEntity.dhis2.json&noop=true" );

        from( "direct:$validate" )
            .setProperty( "questionnaireResponse", body() )
            .to( "fhir://validate/resource?inBody=resource&client=#fhirClient" )
            .setBody( simple( "${body.operationOutcome}" ) )
            .marshal().fhirJson( "R4", true )
            .to( "file://./output?fileName=validate.fhir.json&noop=true" )
            .setBody( simple( "${exchangeProperty.questionnaireResponse}" ));
    }

    private static JacksonDataFormat getJacksonDataFormat( Class<?> klass, boolean prettyPrint )
    {
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat( klass );
        jacksonDataFormat.addModule( new JavaTimeModule() );
        jacksonDataFormat.setPrettyPrint( prettyPrint );

        return jacksonDataFormat;
    }
}
