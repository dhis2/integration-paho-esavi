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
package org.hisp.dhis.integration.esavi.converters.v1;

import static org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED;
import static org.springframework.util.StringUtils.hasText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.hisp.dhis.integration.esavi.domain.tracker.TrackedEntity;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

public final class EsaviProfile
{

    public static QuestionnaireResponse create( TrackedEntity trackedEntity )
    {
        EsaviContext ctx = new EsaviContext( trackedEntity );

        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setId( trackedEntity.getId() );
        response.setAuthored( new Date() );
        response.setStatus( COMPLETED );

        response.setIdentifier( new Identifier()
            .setValue( ctx.attribute( "KSr2yTdu1AI" ) )
            .setSystem( "http://ops.org/esavi/COL" ) );

        response.getMeta()
            .addProfile( "https://paho.org/fhir/esavi/StructureDefinition/ESAVIQuestionnaireResponse" );

        response
            .setQuestionnaire( "https://paho.org/fhir/esavi/Questionnaire/CuestionarioESAVI" );

        response.setText( new Narrative()
            .setStatus( Narrative.NarrativeStatus.GENERATED )
            .setDiv( new XhtmlNode()
                .setValue( "<div>RESPUESTA A CUESTIONARIO ID " + ctx.attribute( "KSr2yTdu1AI" ) + "</div>" ) ) );

        response.addItem( datosNotificacionGeneral( ctx ) );
        response.addItem( patientDemographics( ctx ) );
        response.addItem( pharmaceuticalBackground( ctx ) );
        response.addItem( esaviRegistration( ctx ) );

        return response;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent datosNotificacionGeneral( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "datosNotificacionGeneral" ) );

        item.addItem( datosNotificacion( ctx ) );
        item.addItem( fechas( ctx ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechas( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "fechas" ) );

        item.addItem( fechaConsulta( ctx ) );
        item.addItem( fechaNotificacion( ctx ) );
        item.addItem( fechaLlenadoFicha( ctx ) );
        item.addItem( fechaRepoNacional( ctx ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechaConsulta( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "fechaConsulta" ) );

        item.addAnswer()
            .setValue( new DateType( ctx.dataElement( "PW0dQpcY2wD" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechaNotificacion( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "fechaNotificacion" ) );

        return null;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechaLlenadoFicha( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "fechaLlenadoFicha" ) );

        return null;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechaRepoNacional( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "fechaRepoNacional" ) );

        return null;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent datosNotificacion( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "datosNotificacion" ) );

        item.addItem( caseOriginCountry( ctx ) );
        item.addItem( nombreOrganizacionNotificadora( ctx ) );
        item.addItem( codigoDireccionOrganizacion( ctx ) );
        item.addItem( nombreDireccionOrganizacion( ctx ) );
        item.addItem( codigoProfesionNotificador( ctx ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent caseOriginCountry( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "paisOrigen-Reg" ) );

        // TODO get from TE
        item.addAnswer()
            .setValue( new Coding( "urn:iso:std:iso:3166", "COL", "Columbia" ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent nombreOrganizacionNotificadora(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreOrganizacionNotificadora" ) );

        item.addAnswer()
            .setValue( new StringType( ctx.getEnrollment().getOrgUnitName() ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent codigoDireccionOrganizacion(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "codigoDireccionOrganizacion" ) );

        item.addAnswer()
            .setValue( new Coding( "https://paho.org/fhir/esavi/ValueSet/DirOrgNotiVS", "CO_DC_11001",
                "Bogota, D.C. (Municipio), Santa Fe de Bogota DC, Colombia" ) );

        return null;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent nombreDireccionOrganizacion(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreDireccionOrganizacion" ) );

        item.addAnswer()
            .setValue( new StringType( "Bogota, D.C. (Municipio), Santa Fe de Bogota DC, Colombia" ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent codigoProfesionNotificador(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "codigoProfesionNotificador" ) );

        // 0..1 cardinality in IG
        if ( ctx.hasDataElement( "Tgi4xP5DCzr" ) )
        {
            item.addAnswer()
                    .setValue( EsaviProfession.get( ctx.dataElement( "Tgi4xP5DCzr" ) ) );
        } else { // no data value for that DE, but there is default option in the IG
            item.addAnswer()
                    .setValue( EsaviProfession.get(null) );
        }

        return item;
    }

    // ---------------------------------------------------------------------------------
    // Patient Demographics
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientDemographics( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "datosIdVacunado" ) );

        item.addItem( patientData( ctx ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientData( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "datosPaciente" ) );

        item.addItem( caseId( ctx ) );
        item.addItem( patientId( ctx ) );
        // item.addItem( patientResidenceCode( ctx ) );
        item.addItem( patientResidence( ctx ) );
        item.addItem( patientGender( ctx ) );
        item.addItem( patientDateOfBirth( ctx ) );
        // item.addItem( patientEthnicity( ctx ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent caseId( EsaviContext ctx )
    {
        if ( !ctx.hasAttribute( "KSr2yTdu1AI" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "numeroCaso" ) );

        // TODO fix hardcoding of uid
        item.addAnswer().setValue( new StringType( ctx.attribute( "KSr2yTdu1AI" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientId( EsaviContext ctx )
    {
        if ( !ctx.hasAttribute( "Ewi7FUfcHAD" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "idPaciente" ) );

        // TODO fix hardcoding of uid
        item.addAnswer().setValue( new StringType( hash( ctx.attribute( "Ewi7FUfcHAD" ) ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientResidenceCode( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "codigoResidenciaHabitual" ) );

        // TODO get from TE
        item.addAnswer()
            .setValue( new Coding( "http://paho.org/esavi/CodeSystem/DirOrgNotiCS", "BR_SC_42_05407",
                "Florianópolis (Municipio), Santa Catarina, Brazil" ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientResidence( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreResidenciaHabitual" ) );

        // TODO get from TE
        item.addAnswer().setValue( new StringType( "Florianópolis" ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientGender( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "sexoPaciente" ) );

        // TODO fix hardcoding of uid
        Enumerations.AdministrativeGender gender = EsaviGender.get( ctx.attribute( "oindugucx72" ) );

        item.addAnswer()
            .setValue( new Coding( gender.getSystem(), gender.toCode(), gender.getDisplay() ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientDateOfBirth( EsaviContext ctx )
    {
        if ( !ctx.hasAttribute( "NI0QRzJvQ0k" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "fechaNacimiento" ) );

        // TODO fix hardcoding of uid
        item.addAnswer().setValue( new DateType( ctx.attribute( "NI0QRzJvQ0k" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientEthnicity( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "etnia" ) );

        // TODO get from TE
        item.addAnswer().setValue( new StringType( "TODO" ) );

        return item;
    }

    // ---------------------------------------------------------------------------------
    // Esavi Medicine
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent pharmaceuticalBackground( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "antecedentesFarmacosVacunas" ) );

        // medicamento

        // datosVacunas
        item.addItem( vaccineDataAdministration( ctx, "uSVcZzSM3zg", "JSd0HQOgJ8w", "LIyV4t7eCfZ", "LNqkAlvGplL",
            "VFrc8SNFYm7" ) );
        item.addItem( vaccineDataAdministration( ctx, "g9PjywVj2fs", "eRwc8Y0CNLh", "E3F414izniN", "b1rSwGRcY5W",
            "rVUo2PBgwhr" ) );
        item.addItem( vaccineDataAdministration( ctx, "OU5klvkk3SM", "wdZrkUvnuyr", "WlE0K4xCc14", "YBnFoNouH6f",
            "ffYfdSPmM1W" ) );
        item.addItem( vaccineDataAdministration( ctx, "menOXwIFZh5", "Ptms0lmt4QX", "Aya8C25DXHe", "BHAfwo6JPDa",
            "ZfjyIKeX1AN" ) );

        // datosVacunacion
        item.addItem( vaccineData( ctx, "uSVcZzSM3zg", "dOkuCjpD978", "BSUncNBb20j", "om7AsREDduc", "zIKVrYHtdUx" ) );
        item.addItem( vaccineData( ctx, "g9PjywVj2fs", "VrzEutEnzSJ", "fZFQVZFqu0q", "xXjnT9sjt4F", "KTHsZhIAGWf" ) );
        item.addItem( vaccineData( ctx, "OU5klvkk3SM", "f4WCAVwjHz0", "VQKdZ1KeD7u", "fW6RbpJk4hS", "gG0FZYpEctJ" ) );
        item.addItem( vaccineData( ctx, "menOXwIFZh5", "H3TKHMFIN6V", "S1PRFSk8Y9v", "va0Smpy0LUn", "EDdd0HsfLcO" ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministration( EsaviContext ctx,
        String id, String brandName, String doses, String batch, String expiryDate )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "datosVacunas" ) );

        item.addItem( vaccineDataAdministrationName( ctx, id ) );
        item.addItem( vaccineDataAdministrationBrandName( ctx, brandName ) );
        item.addItem( vaccineDataAdministrationIdentifier( ctx, id ) );
        item.addItem( vaccineDataAdministrationDoses( ctx, doses ) );
        item.addItem( vaccineDataAdministrationBatch( ctx, batch ) );
        item.addItem( vaccineDataAdministrationExpiryDate( ctx, expiryDate ) );

        // move to medicine
        // item.addItem( vaccineDataAdministrationCode( ctx, id ) );

        // move to medicine, invalid option
        // item.addItem( vaccineDataAdministrationSystemWhoDrug( ctx ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationName(
        EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreVacuna" ) );

        item.addAnswer()
            .setValue( new StringType( ctx.option( "PrAA7nJPXke", ctx.dataElement( id ), id ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationCode(
        EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "codigoMedicamento" ) );

        item.addAnswer()
            .setValue( new Coding( "https://paho.org/fhir/esavi/ValueSet/CodigoMedicamentoVS",
                ctx.dataElement( id ), ctx.option( "PrAA7nJPXke", ctx.dataElement( id ), id ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationBrandName(
        EsaviContext ctx, String brandName )
    {
        if ( !ctx.hasDataElement( brandName ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreNormalizadoVacuna" ) );

        item.addAnswer()
            .setValue( new StringType( ctx.dataElement( brandName ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationIdentifier(
        EsaviContext ctx, String id )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "identificadorVacuna" ) );

        // TODO value from DHIS2 is a long not integer
        // https://dev.paho-dhis2.org/api/optionSets/PrAA7nJPXke?fields=id,code,name,options[id,code,name]
        Integer value = Integer.valueOf( "132" );

        item.addAnswer()
            .setValue( new IntegerType( value ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationSystemWhoDrug(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "sistemaDeCodificacionParaNombreNormalizadoMedicamento" ) );

        item.addAnswer()
            .setValue(
                new Coding( "https://paho.org/fhir/esavi/CodeSystem/SistemasDeCodificacionCS", "2", "WHODrug" ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationDoses(
        EsaviContext ctx, String doses )
    {
        if ( !ctx.hasDataElement( doses ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "numeroDosisVacuna" ) );

        item.addAnswer()
            .setValue( new IntegerType( ctx.dataElement( doses ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationBatch(
        EsaviContext ctx, String batch )
    {
        if ( !ctx.hasDataElement( batch ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "numeroLote" ) );

        item.addAnswer()
            .setValue( new StringType( ctx.dataElement( batch ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationExpiryDate(
        EsaviContext ctx,
        String expiryDate )
    {
        if ( !ctx.hasDataElement( expiryDate ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "fechaVencimientoVacuna" ) );

        item.addAnswer()
            .setValue( new DateType( ctx.dataElement( expiryDate ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineData( EsaviContext ctx,
        String id, String vaccineDate, String vaccineTime, String reconstitutionDate, String reconstitutionTime )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "datosVacunacion" ) );

        item.addItem( vaccineDataVaccinationSite( ctx, id ) );
        item.addItem( vaccineDataDate( ctx, vaccineDate ) );
        item.addItem( vaccineDataTime( ctx, vaccineTime ) );
        item.addItem( vaccineDataAddress( ctx ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataVaccinationSite(
        EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreVacunatorio" ) );

        item.addAnswer()
            .setValue( new StringType( ctx.getEnrollment().getOrgUnitName() ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataDate( EsaviContext ctx,
        String vaccineDate )
    {
        if ( !ctx.hasDataElement( vaccineDate ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "fechaVacunacion" ) );

        item.addAnswer()
            .setValue( new DateType( ctx.dataElement( vaccineDate ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataTime( EsaviContext ctx,
        String vaccineTime )
    {
        if ( !ctx.hasDataElement( vaccineTime ) )
        {
            return null;
        }

        String localTime = LocalTime.parse( ctx.dataElement( vaccineTime ) )
            .format( DateTimeFormatter.ISO_TIME );

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "horaVacunacion" ) );

        item.addAnswer()
            .setValue( new TimeType( localTime ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAddress(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreDireccionVacunatorio" ) );

        item.addAnswer()
            .setValue( new StringType( "Complete Address Where Given" ) );

        return item;
    }

    // ---------------------------------------------------------------------------------
    // Esavi Registrations
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviRegistration(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "registroESAVI" ) );

        item.addItem( esaviData( ctx, "PZxZirhNzgS", "1", "LYariSd5cEq", "mqCTfs4jXSo", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "maY0Vi68Fv9", "2", "hfdzpv7lP6C", "hc15z2mXm2o", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "Sy1uqYvgR3r", "3", "oHVQ23x5NQE", "DBV8wfaQCMt", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "Og99AH5tIQz", "4", "OGRWlduylFk", "NuwfTxCxvca", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "vqf60JfNqsf", "5", "QeXeXYdBAUE", "XQjZ1N8dNkt", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "pQJc4VA2SDW", "6", "MfgJjmoOdxm", "kDgoKxw8sVJ", "ci3S3BH6wZn" ) );

        item.addItem( esaviSeriousness( ctx ) );
        item.addItem( esaviOutcome( ctx ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviData( EsaviContext ctx, String id,
        String position, String startDate, String startTime, String description )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "datosESAVI" ) );

        item.addItem( esaviName( ctx, id ) );
        item.addItem( esaviPosition( ctx, position ) );
        item.addItem( esaviMeddraCode( ctx, id ) );
        item.addItem( esaviStartDate( ctx, startDate ) );
        item.addItem( esaviStartTime( ctx, startTime ) );
        item.addItem( esaviDescription( ctx, description ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviName( EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) || !ctx.hasOption( "OzARj1D09Dm", ctx.dataElement( id ) ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreESAVI" ) );

        item.addAnswer().setValue( new StringType( ctx.option( "OzARj1D09Dm", ctx.dataElement( id ) ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviPosition( EsaviContext ctx,
        String position )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "IdentificadorESAVI" ) );

        item.addAnswer().setValue( new IntegerType( position ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviMeddraCode( EsaviContext ctx,
        String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "codigoESAVIMedDRA" ) );

        String display = ctx.option( "OzARj1D09Dm", ctx.dataElement( id ), "" );

        item.addAnswer()
            .setValue(
                new Coding( "https://paho.org/fhir/esavi/CodeSystem/EsaviMedDRACS", ctx.dataElement( id ), display ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviStartDate( EsaviContext ctx,
        String startDate )
    {
        if ( !ctx.hasDataElement( startDate ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "fechaESAVI" ) );

        item.addAnswer().setValue( new DateType( ctx.dataElement( startDate ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviStartTime( EsaviContext ctx,
        String startTime )
    {
        if ( !ctx.hasDataElement( startTime ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "horaESAVI" ) );

        item.addAnswer()
            .setValue( new DateType( ctx.dataElement( startTime ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviDescription( EsaviContext ctx,
        String description )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "descripcionESAVI" ) );

        item.addAnswer()
            .setValue( new StringType( ctx.dataElement( description ) ) );

        return item;
    }

    // ---------------------------------------------------------------------------------
    // ESAVI Seriousness
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousness(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "gravedadESAVI" ) );

        item.addItem( esaviSeriousnessSevere( ctx ) );

        if ( ctx.dataElementIsTrue( "fq1c1A3EOX5" ) )
        {
            item.addItem( esaviSeriousnessDeath( ctx ) );
            item.addItem( esaviSeriousnessLifeThreatening( ctx ) );
            item.addItem( esaviSeriousnessDisability( ctx ) );
            item.addItem( esaviSeriousnessHospitalization( ctx ) );
            item.addItem( esaviSeriousnessCongenitalAnomaly( ctx ) );
            item.addItem( esaviSeriousnessAbortion( ctx ) );
            item.addItem( esaviSeriousnessFetalDeath( ctx ) );
            item.addItem( esaviSeriousnessOther( ctx ) );

            if ( ctx.hasDataElement( "TKikUtqJQTq" ) )
            {
                item.addItem( esaviSeriousnessOtherDescription( ctx ) );
            }
        }

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessSevere(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "tipoGravedad" ) );

        item.addAnswer()
            .setValue( new BooleanType( ctx.dataElementAsBoolean( "fq1c1A3EOX5" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessDeath(
        EsaviContext ctx )
    {
        if ( !ctx.dataElementIsTrue( "DOA6ZFMro84" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "gravMuerte" ) );

        item.addAnswer()
            .setValue( new BooleanType( ctx.dataElementAsBoolean( "DOA6ZFMro84" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessLifeThreatening(
        EsaviContext ctx )
    {
        if ( !ctx.dataElementIsTrue( "lATDYNmTLKD" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "gravRiesgoVida" ) );

        item.addAnswer()
            .setValue( new BooleanType( ctx.dataElementAsBoolean( "lATDYNmTLKD" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessDisability(
        EsaviContext ctx )
    {
        if ( !ctx.dataElementIsTrue( "lsO8n8ZmLAB" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "gravDiscapacidad" ) );

        item.addAnswer()
            .setValue( new BooleanType( ctx.dataElementAsBoolean( "lsO8n8ZmLAB" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessHospitalization(
        EsaviContext ctx )
    {
        if ( !ctx.dataElementIsTrue( "Il1lTfknLdd" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "gravHospitalizacion" ) );

        item.addAnswer().setValue( new BooleanType( ctx.dataElementAsBoolean( "Il1lTfknLdd" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessCongenitalAnomaly(
        EsaviContext ctx )
    {
        if ( !ctx.dataElementIsTrue( "lSBsxcQU0kO" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "gravAnomaliaCongenita" ) );

        item.addAnswer()
            .setValue( new BooleanType( ctx.dataElementAsBoolean( "lSBsxcQU0kO" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessAbortion(
        EsaviContext ctx )
    {
        if ( !ctx.dataElementIsTrue( "ggjKwDKEwbP" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "gravAborto" ) );

        item.addAnswer().setValue( new BooleanType( ctx.dataElementAsBoolean( "ggjKwDKEwbP" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessFetalDeath(
        EsaviContext ctx )
    {
        if ( !ctx.dataElementIsTrue( "M3nwvMsaaWJ" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "gravMuerteFetal" ) );

        item.addAnswer()
            .setValue( new BooleanType( ctx.dataElementAsBoolean( "M3nwvMsaaWJ" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessOther(
        EsaviContext ctx )
    {
        if ( !ctx.dataElementIsTrue( "VXdRoWQOBxG" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "otrosEventosImportantes" ) );

        item.addAnswer()
            .setValue( new BooleanType( ctx.dataElementAsBoolean( "VXdRoWQOBxG" ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviSeriousnessOtherDescription(
        EsaviContext ctx )
    {
        if ( !ctx.hasDataElement( "TKikUtqJQTq" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "otrosEventosImportantesTx" ) );

        item.addAnswer().setValue( new StringType( ctx.dataElement( "TKikUtqJQTq" ) ) );

        return item;
    }

    // ---------------------------------------------------------------------------------
    // ESAVI Outcome
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviOutcome( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "desenlaceESAVI" ) );

        item.addItem( esaviOutcomeCode( ctx ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviOutcomeCode(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "codDesenlaceESAVI" ) );

        EsaviOutcomeCode outcomeCode = EsaviOutcomeCode.UNKNOWN;

        try
        {
            String value = ctx.dataElement( "yRrSDiR5v1M" );

            if ( hasText( value ) )
            {
                outcomeCode = EsaviOutcomeCode.valueOf( value.toUpperCase() );
            }

        }
        catch ( IllegalArgumentException ignored )
        {
        }

        item.addAnswer()
            .setValue( new Coding( outcomeCode.getSystem(), outcomeCode.getCode(), outcomeCode.getDisplay() ) );

        return item;
    }

    // ---------------------------------------------------------------------------------
    // Common
    // ---------------------------------------------------------------------------------

    private static String hash( String value )
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            md.update( value.getBytes() );
            byte[] digest = md.digest();

            StringBuilder hexString = new StringBuilder();

            for ( byte b : digest )
            {
                hexString.append( String.format( "%02x", b & 0xff ) );
            }

            return hexString.toString();
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }
    }

    private EsaviProfile()
    {

    }
}
