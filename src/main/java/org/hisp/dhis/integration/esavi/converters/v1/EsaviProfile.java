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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hisp.dhis.api.model.v2_38_1.TrackedEntity;
import org.hisp.dhis.integration.esavi.config.properties.DhisProperties;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

public final class EsaviProfile {

    public static final String OPTIONSET_WHODRUG_COVID = "PrAA7nJPXke";
    public static final String OPTIONSET_MEDDRA = "OzARj1D09Dm";
    public static final String OPTIONSET_DILUENTS = "NdEeGMVaObK";
    public static final String OPTIONSET_DISTRITOS = "TYYlo7IdrCw";
    public static final String OPTIONSET_LUGAR_VACUNACION = "GUpt2UXm3hR";
    public static final String VACCINE_1 = "uSVcZzSM3zg";
    public static final String VACCINE_2 = "g9PjywVj2fs";
    public static final String VACCINE_3 = "OU5klvkk3SM";
    public static final String VACCINE_4 = "menOXwIFZh5";

    public static final String CAUSALITY_A1 = "tIHAJAXcDev";
    public static final String CAUSALITY_A2 = "vIk1r0MRmBh";
    public static final String CAUSALITY_A3 = "bNjv960SqRX";
    public static final String CAUSALITY_A4 = "NsjdL0Z0fIn";
    public static final String CAUSALITY_B1 = "dzYVg6O3ms5";
    public static final String CAUSALITY_B2 = "pA2fuBPhwX7";
    public static final String CAUSALITY_C = "pJk4Slb5EFb";
    public static final String CAUSALITY_D = "YC286LMPlQW";

    private EsaviProfile()
    {

    }

    public static QuestionnaireResponse create(TrackedEntity trackedEntity, DhisProperties dhisProperties) {
        EsaviContext ctx = new EsaviContext(trackedEntity, dhisProperties);

        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setId(trackedEntity.getTrackedEntity().get());
        try {
            Date authored = new SimpleDateFormat("yyyy-MM-dd").parse(ctx.getCompletedDate());
            response.setAuthored(authored);
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        response.setStatus(COMPLETED);

        response.setIdentifier(new Identifier()
                .setValue(ctx.attribute("KSr2yTdu1AI"))
                .setSystem("http://ops.org/esavi/PRY"));

        response.getMeta()
                .addProfile("https://paho.org/fhir/esavi/StructureDefinition/ESAVIQuestionnaireResponse");

        response
                .setQuestionnaire("https://paho.org/fhir/esavi/Questionnaire/CuestionarioESAVI");

        response.setText(new Narrative()
                .setStatus(Narrative.NarrativeStatus.GENERATED)
                .setDiv(new XhtmlNode()
                        .setValue("<div>RESPUESTA A CUESTIONARIO ID " + ctx.attribute("KSr2yTdu1AI") + "</div>")));

        response.addItem(datosNotificacionGeneral(ctx));
        response.addItem(patientDemographics(ctx));
        response.addItem(medicalBackground(ctx));
        response.addItem(pharmaceuticalBackground(ctx));
        response.addItem(esaviRegistration(ctx));

        return response;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent datosNotificacionGeneral(EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("datosNotificacionGeneral"));

        item.addItem(datosNotificacion(ctx));
        item.addItem(fechas(ctx));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechas(EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("fechas"));

        item.addItem(fechaConsulta(ctx));
        item.addItem(fechaNotificacion(ctx));
        item.addItem(fechaLlenadoFicha(ctx));
        item.addItem(fechaRepoNacional(ctx));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechaConsulta(EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("fechaConsulta"));

        item.addAnswer()
                .setValue(new DateType(ctx.dataElement("PW0dQpcY2wD")));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechaNotificacion(EsaviContext ctx) {
        if (ctx.getCompletedDate() == null) {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("fechaNotificacion"));

        item.addAnswer()
                .setValue(new DateType(ctx.getCompletedDate()));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechaLlenadoFicha(EsaviContext ctx) {
        if (ctx.getCompletedDate() == null) {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("fechaLlenadoFicha"));

        item.addAnswer()
                .setValue(new DateType(ctx.getCompletedDate()));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechaRepoNacional(EsaviContext ctx) {
        if (ctx.getCompletedDate() == null) {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("fechaRepoNacional"));

        item.addAnswer()
                .setValue(new DateType(ctx.getCompletedDate()));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent datosNotificacion(EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("datosNotificacion"));

        item.addItem(caseOriginCountry(ctx));
        item.addItem(nombreOrganizacionNotificadora(ctx));
        item.addItem(codigoDireccionOrganizacion(ctx));
        item.addItem(nombreDireccionOrganizacion(ctx));
        item.addItem(codigoProfesionNotificador(ctx));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent caseOriginCountry(EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("paisOrigen-Reg"));

        // TODO (Future) Update source of this field. Currently fixed to Paraguay (PRY)
        item.addAnswer()
                .setValue(new Coding("https://paho.org/fhir/esavi/CodeSystem/codPaisesCS", "PRY", "Paraguay"));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent nombreOrganizacionNotificadora(
            EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("nombreOrganizacionNotificadora"));

        item.addAnswer()
                .setValue(new StringType(ctx.getEnrollment().getOrgUnitName().get()));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent codigoDireccionOrganizacion(
            EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("codigoDireccionOrganizacion"));

        item.addAnswer()
                .setValue(new Coding("https://paho.org/fhir/esavi/ValueSet/DirOrgNotiVS", "CO_DC_11001",
                        "Bogota, D.C. (Municipio), Santa Fe de Bogota DC, Colombia"));
        // TODO As the mapping is pending, the returning value is null
        return null;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent nombreDireccionOrganizacion(
            EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("nombreDireccionOrganizacion"));

        item.addAnswer()
                .setValue(new StringType("Bogota, D.C. (Municipio), Santa Fe de Bogota DC, Colombia"));

        return item;
    }

    // ---------------------------------------------------------------------------------
    // Patient Demographics
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent codigoProfesionNotificador(
            EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("codigoProfesionNotificador"));

        // 0..1 cardinality in IG
        if (ctx.hasDataElement("Tgi4xP5DCzr")) {
            item.addAnswer()
                    .setValue(EsaviProfession.get(ctx.dataElement("Tgi4xP5DCzr")));
        } else { // no data value for that DE, but there is default option in the IG
            item.addAnswer()
                    .setValue(EsaviProfession.get(null));
        }

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientDemographics(EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("datosIdVacunado"));

        item.addItem(patientData(ctx));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientData(EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("datosPaciente"));

        // numeroCaso
        item.addItem(caseId(ctx));

        // idPaciente
        item.addItem(patientId(ctx));

        // codigoResidenciaHabitual
        // no mapping

        // nombreResidenciaHabitual
        item.addItem(patientResidence(ctx));

        // sexoPaciente
        item.addItem(patientGender(ctx));

        // fechaNacimiento
        item.addItem(patientDateOfBirth(ctx));

        // etnia
        // no mapping

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent caseId(EsaviContext ctx) {
        if (!ctx.hasAttribute("KSr2yTdu1AI")) {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("numeroCaso"));

        // TODO fix hardcoding of uid
        item.addAnswer().setValue(new StringType(ctx.attribute("KSr2yTdu1AI")));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientId(EsaviContext ctx) {
        if (!ctx.hasAttribute("Ewi7FUfcHAD")) {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("idPaciente"));

        // TODO fix hardcoding of uid
        item.addAnswer().setValue(new StringType(hash(ctx.attribute("Ewi7FUfcHAD"))));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientResidence(EsaviContext ctx) {

        final String TEA_RESIDENCIA = "eISp65Kw0Z7";

        if (!ctx.hasAttribute(TEA_RESIDENCIA)) {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("nombreResidenciaHabitual"));

        item.addAnswer().setValue(new StringType(ctx.option(OPTIONSET_DISTRITOS, ctx.attribute(TEA_RESIDENCIA))));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientGender(EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("sexoPaciente"));

        // TODO fix hardcoding of uid
        Enumerations.AdministrativeGender gender = EsaviGender.get(ctx.attribute("oindugucx72"));

        item.addAnswer()
                .setValue(new Coding(gender.getSystem(), gender.toCode(), gender.getDisplay()));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent patientDateOfBirth(EsaviContext ctx) {
        if (!ctx.hasAttribute("NI0QRzJvQ0k")) {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("fechaNacimiento"));

        // TODO fix hardcoding of uid
        item.addAnswer().setValue(new DateType(ctx.attribute("NI0QRzJvQ0k")));

        return item;
    }

    // ---------------------------------------------------------------------------------
    // Esavi medicalBackground
    // ---------------------------------------------------------------------------------


    private static QuestionnaireResponse.QuestionnaireResponseItemComponent medicalBackground(EsaviContext ctx) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("antecedentesMedicos"));

        // ensayoClinico
        // No mapping

        // antecedentesEnfermedadesPrevias
        item.addItem( antecedentesEnfermedadesPrevias( ctx ) );

        // antecedentesEventosAdversos
        item.addItem(antecedentesEventosAdversos(ctx));

        // antecedentesSarsCov2
        item.addItem(antecedentesSarsCov2(ctx));

        // pacienteEmbarazada
        item.addItem(pacienteEmbarazada(ctx));

        if (!item.hasItem()) {
            return null;
        }

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent antecedentesEnfermedadesPrevias(
            EsaviContext ctx) {

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("antecedentesEnfermedadesPrevias"));

        String[] medical_history_uids = {"qefbRP79xOR", "AFZZf15RB9H", "IHAuvjbCaiq", "q5gX7VOf0LI", "j6J8gLoFePq", "Fm78gKjGygn", "ZKn2LDznlHd", "FUxdYjcINIh", "j9yee5ZTdyE"};
        for (String medical_history : medical_history_uids) {
            if (ctx.hasDataElement(medical_history)) {
                String descripcionEnfPreviaStr = ctx.option(OPTIONSET_MEDDRA, ctx.dataElement(medical_history));
                QuestionnaireResponse.QuestionnaireResponseItemComponent descripcionEnfPrevia = new QuestionnaireResponse.QuestionnaireResponseItemComponent(new StringType("descripcionEnfPrevia"));
                descripcionEnfPrevia.addAnswer().setValue(new StringType(descripcionEnfPreviaStr));
                item.addItem(descripcionEnfPrevia);

                QuestionnaireResponse.QuestionnaireResponseItemComponent codigoMedDRAEnfPrevia = new QuestionnaireResponse.QuestionnaireResponseItemComponent(new StringType("codigoMedDRAEnfPrevia"));
                codigoMedDRAEnfPrevia.addAnswer().setValue(EsaviMeddra.get(ctx.dataElement(medical_history), descripcionEnfPreviaStr));
                item.addItem(codigoMedDRAEnfPrevia);
            }

        }

        if (!item.hasItem()) {
            return null;
        }

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent antecedentesEventosAdversos(
            EsaviContext ctx) {

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("antecedentesEventosAdversos"));

        String EVENTO_SIMILAR = "IdCrdz34ZBK"; // ESAVI - Evento similar
        if (ctx.hasDataElement(EVENTO_SIMILAR)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemInside = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType("antecedentesAdvSimilar"));
            itemInside.addAnswer().setValue(EsaviRespuestaSimple.get(ctx.dataElement(EVENTO_SIMILAR)));
            item.addItem(itemInside);
        }

        String ALERGIA_MEDICAMENTOS = "rgVs3pWqzx2"; // ESAVI - Alergia Medicamentos
        if (ctx.hasDataElement(ALERGIA_MEDICAMENTOS)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemInside = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType("alergiaMedicamentos"));
            itemInside.addAnswer().setValue(EsaviRespuestaSimple.get(ctx.dataElement(ALERGIA_MEDICAMENTOS)));
            item.addItem(itemInside);
        }

        String ALERGIA_VACUNA = "CywpFDbxPqH"; // ESAVI - Alergia vacuna
        if (ctx.hasDataElement(ALERGIA_MEDICAMENTOS)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemInside = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType("alergiaVacunas"));
            itemInside.addAnswer().setValue(EsaviRespuestaSimple.get(ctx.dataElement(ALERGIA_VACUNA)));
            item.addItem(itemInside);
        }

        if (!item.hasItem()) {
            return null;
        }

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent antecedentesSarsCov2(EsaviContext ctx) {

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("antecedentesSarsCov2"));

        // diagnosticoprevioSarsCov2
        String ANTECEDENTE_COVID = "XBU8oloqd7i"; // ESAVI - Antecedente COVID
        if (ctx.hasDataElement(ANTECEDENTE_COVID)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemInside = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType("diagnosticoprevioSarsCov2"));
            itemInside.addAnswer().setValue(EsaviRespuestaSimple.get(ctx.dataElement(ANTECEDENTE_COVID)));
            item.addItem(itemInside);
        }

        // asintomaticoSars
        // No mapping

        // fechaSintomasCovid19
        // No mapping

        // tipoConfirmacionCovid19
        // No mapping

        // fechaTomaMuestraCovid19
        // No mapping

        if (!item.hasItem()) {
            return null;
        }

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent pacienteEmbarazada(EsaviContext ctx) {

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType("pacienteEmbarazada"));

        // embarazadaMomentoVacuna
        String ESTA_EMBARAZADA_VACUNA = "U19JzF3LjsS"; // ESAVI - Embarazada en la vacunación
        if (ctx.hasDataElement(ESTA_EMBARAZADA_VACUNA)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemInside = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType("embarazadaMomentoVacuna"));
            itemInside.addAnswer().setValue(EsaviRespuestaSimple.get(ctx.dataElement(ESTA_EMBARAZADA_VACUNA)));
            item.addItem(itemInside);
        }

        // embarazadaMomentoESAVI
        String ESTA_EMBARAZADA_ESAVI = "ZzoWAqln5xc"; // ESAVI - Embarazada al inicio ESAVI
        if (ctx.hasDataElement(ESTA_EMBARAZADA_ESAVI)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemInside = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType("embarazadaMomentoESAVI"));
            itemInside.addAnswer().setValue(EsaviRespuestaSimple.get(ctx.dataElement(ESTA_EMBARAZADA_ESAVI)));
            item.addItem(itemInside);
        }

        // fechaUltimaMenstruacion
        String FECHA_ULTIMA_MENSTRUACION = "oCKpt0i7VeZ"; // ESAVI - Fecha última menstruación
        if (ctx.hasDataElement(FECHA_ULTIMA_MENSTRUACION)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemInside = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType("fechaUltimaMenstruacion"));
            itemInside.addAnswer().setValue(new DateType(ctx.dataElement(FECHA_ULTIMA_MENSTRUACION)));
            item.addItem(itemInside);
        }

        // fechaProbableParto
        String FECHA_PROBABLE_PARTO = "mfGQRlcG7cc"; // ESAVI - Fecha probable de parto
        if (ctx.hasDataElement(FECHA_PROBABLE_PARTO)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemInside = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType("fechaProbableParto"));
            itemInside.addAnswer().setValue(new DateType(ctx.dataElement(FECHA_PROBABLE_PARTO)));
            item.addItem(itemInside);
        }

        // edadGestacional
        // No mapping

        // monitoreoPosteriorVacuna
        String MONITOREO_POST_VACUNA = "Nl96399itF0"; // ESAVI - Seguimiento gestante
        if (ctx.hasDataElement(MONITOREO_POST_VACUNA)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemInside = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType("monitoreoPosteriorVacuna"));
            itemInside.addAnswer().setValue(EsaviRespuestaSimple.fromBoolean(Boolean.parseBoolean(ctx.dataElement(MONITOREO_POST_VACUNA))));
            item.addItem(itemInside);
        }

        if (!item.hasItem()) {
            return null;
        }

        return item;
    }

    // ---------------------------------------------------------------------------------
    // Esavi Medicine
    // ---------------------------------------------------------------------------------
    private static boolean moreThanOneVaccine(EsaviContext ctx) {

        String[] vaccine_ids = {VACCINE_1, VACCINE_2, VACCINE_3, VACCINE_4};
        int vaccines = 0;
        for (String vaccine_id : vaccine_ids)
        {
            if ( ctx.hasDataElement( vaccine_id ) )
            {
                ++vaccines;
            }
        }

        if (vaccines>1){
            return true;
        } else {
            return false;
        }

    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent pharmaceuticalBackground( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "antecedentesFarmacosVacunas" ) );

        // medicamento
        item.addItem( medicamento( ctx, "YDhHKT2hE8j", "LaStdK115NF", "B9HiK1fADgK" ) );
        item.addItem( medicamento( ctx, "YzZ5iOPzR6k", "cBKqulUmt9b", "FKgkFwKpjfu" ) );
        item.addItem( medicamento( ctx, "i7ylwQssbZs", "wNzChKbsxd0", "QzkGC9PeXNe" ) );
        item.addItem( medicamento( ctx, "xbrWBpcL7Mc", "kxFDJmHFX2j", "SznBvVkfQxc" ) );
        item.addItem( medicamento( ctx, "CvJTcYvJxMX", "CMSNZVmLxGq", "pymdeJkXNWZ" ) );
        item.addItem( medicamento( ctx, "j69skZQLxJR", "HFnr2nf6VC6", "mBJnveQPhMK" ) );
        item.addItem( medicamento( ctx, "HAz2UIdgtPe", "aHFjm75ialS", "fgoMOIvotYF" ) );
        item.addItem( medicamento( ctx, "lwSV5ilPBbQ", "rqg6Z6aOU20", "GqdK5VSSC0q" ) );
        item.addItem( medicamento( ctx, "VidbwCnSw2X", "ZTlbQp6AUxR", "LI1ea2cTRNw" ) );
        item.addItem( medicamento( ctx, "nKWV4cjQ9lR", "p7VnQrQyGEl", "eEmvhkIOSKm" ) );

        // datosVacunas
        item.addItem( vaccineDataAdministration( ctx, VACCINE_1, "JSd0HQOgJ8w", "LIyV4t7eCfZ", "LNqkAlvGplL",
            "VFrc8SNFYm7", "dOkuCjpD978", "BSUncNBb20j", "om7AsREDduc", "zIKVrYHtdUx", "xk9QvZPMVQF", "FQM2ksIQix8", "cKx0VCmLrsc" ) );
        item.addItem( vaccineDataAdministration( ctx, VACCINE_2, "eRwc8Y0CNLh", "E3F414izniN", "b1rSwGRcY5W",
            "rVUo2PBgwhr", "VrzEutEnzSJ", "fZFQVZFqu0q", "xXjnT9sjt4F", "KTHsZhIAGWf", "WN8844HG0zi", "ufWU3WStZgG", "FcqNLPNUPId" ) );
        item.addItem( vaccineDataAdministration( ctx, VACCINE_3, "wdZrkUvnuyr", "WlE0K4xCc14", "YBnFoNouH6f",
            "ffYfdSPmM1W", "f4WCAVwjHz0", "VQKdZ1KeD7u", "fW6RbpJk4hS", "gG0FZYpEctJ", "pLu0luPWikb", "MLP8fi1X7UX", "MGjnXmtmd7l" ) );
        item.addItem( vaccineDataAdministration( ctx, VACCINE_4, "Ptms0lmt4QX", "Aya8C25DXHe", "BHAfwo6JPDa",
            "ZfjyIKeX1AN", "H3TKHMFIN6V", "S1PRFSk8Y9v", "va0Smpy0LUn", "EDdd0HsfLcO", "ZTyN8vSf7bc", "MyWtDaOdlyD", "qhDonTAIjl0" ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent medicamento( EsaviContext ctx,
                                                                                         String medicamento_id, String formaFarmacetica, String viaAdministracion )
    {
        if ( !ctx.hasDataElement( medicamento_id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "medicamento" ) );

        // nombreMedicamento
        item.addItem( medicamentoNombre( ctx, medicamento_id ) );

        // sistemaDeCodificacionParaNombreNormalizadoMedicamento
        // item.addItem( sistemaDeCodificacionParaNombreNormalizadoMedicamento( ctx ) );

        // nombreNormalizadoMedicamento
        // no mapping

        // codigoMedicamento
        item.addItem( medicamentoCodigo( ctx, medicamento_id ) );

        // nombreFormaFarmaceutica
        item.addItem( medicamentoFormaFarmaceutica( ctx, formaFarmacetica ) );

        // codigoFormaFarmaceutica
        // no mapping

        // nombreViaAdministracion
        item.addItem( medicamentoViaAdministracion( ctx, viaAdministracion ) );

        // codigoViaAdministracion
        // no mapping

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent medicamentoNombre(EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "nombreMedicamento" ) );

        item.addAnswer().setValue( new StringType( ctx.option( "deNBd8tEIeD", ctx.dataElement( id ) ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent sistemaDeCodificacionParaNombreNormalizadoMedicamento(
            EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "sistemaDeCodificacionParaNombreNormalizadoMedicamento" ) );

        item.addAnswer()
                .setValue(
                        new Coding( "https://paho.org/fhir/esavi/CodeSystem/SistemasDeCodificacionCS", "2", "WHODrug" ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent medicamentoCodigo(EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "codigoMedicamento" ) );

        String display = ctx.option( "deNBd8tEIeD", ctx.dataElement( id ) );
        item.addAnswer().setValue(EsaviWhoDrug.get(ctx.dataElement( id ), display));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent medicamentoFormaFarmaceutica(EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "nombreFormaFarmaceutica" ) );

        item.addAnswer().setValue( new StringType( ctx.option( "qRyur64ZaPK", ctx.dataElement( id ) ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent medicamentoViaAdministracion(EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "nombreViaAdministracion" ) );

        item.addAnswer().setValue( new StringType( ctx.option( "E9d1xL5jsTJ", ctx.dataElement( id ) ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministration( EsaviContext ctx,
        String id, String manufacturerName, String doses, String batch, String expiryDate, String vaccineDate, String vaccineTime, String reconstitutionDate, String reconstitutionTime, String diluentName, String diluentBatch, String diluentExpiryDate)
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "datosVacunas" ) );

        // nombreVacuna
        item.addItem( vaccineDataAdministrationName( ctx, id ) );

        // sistemaDeCodificacionParaNombreNormalizadoVacuna
        item.addItem( vaccineDataSistemaDeCodificacionParaNombreNormalizadoVacuna( ctx, id ) );

        // nombreNormalizadoVacuna
        item.addItem( vaccineDataNombreNormalizadoVacuna( ctx, id ) );


        // identificadorVacuna
        item.addItem( vaccineDataAdministrationIdentifier( ctx, id ) );


        // codigoVacunaWHODrug
        item.addItem( codigoVacunaWHODrug( ctx, id ) );

        // codigoVacunaOtro
        // no mapping

        // nombreFabricante
        item.addItem( vaccineDataAdministrationManufacturerName( ctx, manufacturerName ) );

        // codigoFabricanteWHODrug
        // no mapping

        // numeroDosisVacuna
        item.addItem( vaccineDataAdministrationDoses( ctx, doses ) );

        // numeroLote
        item.addItem( vaccineDataAdministrationBatch( ctx, batch ) );

        // fechaVencimientoVacuna
        item.addItem( vaccineDataAdministrationExpiryDate( ctx, expiryDate ) );

        // nombreDiluyenteVacuna
        item.addItem( vaccineDataAdministrationDiluentName( ctx, diluentName ) );

        // numeroLoteDiluyente
        item.addItem( vaccineDataAdministrationDiluentBatch( ctx, diluentBatch ) );

        // fechaVencimientoDiluyente
        item.addItem( vaccineDataAdministrationDiluentExpiryDate( ctx, diluentExpiryDate ) );

        // nombreVacunatorio
        item.addItem( vaccineDataVaccinationSite( ctx ) );

        // fechaVacunacion
        item.addItem( vaccineDataDate( ctx, vaccineDate ) );

        // horaVacunacion
        item.addItem( vaccineDataTime( ctx, vaccineTime ) );

        // codigoDireccionVacunatorio
        // item.addItem( vaccineDataAddress( ctx ) );
        // no mapping

        // nombreDireccionVacunatorio
        // no mapping

        // codigoMecanismoVerificacion
        item.addItem( vaccineDataCodigoMecanismoVerificacion( ctx, id ) );

        // nombreOtroMecanismoVerificacion
        item.addItem( vaccineDataNombreOtroMecanismoVerificacion( ctx, id ) );

        // fechaReconstitucionVacuna
        item.addItem( vaccineDataReconstitutionDate( ctx, reconstitutionDate ) );

        // horaReconstitucionVacuna
        item.addItem( vaccineDataReconstitutionTime( ctx, reconstitutionTime ) );

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
            .setValue( new StringType( ctx.option(OPTIONSET_WHODRUG_COVID, ctx.dataElement( id ), id ) ) );

        return item;
    }


    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataSistemaDeCodificacionParaNombreNormalizadoVacuna(
            EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "sistemaDeCodificacionParaNombreNormalizadoVacuna" ) );

        item.addAnswer()
                .setValue(
                        new Coding( "https://paho.org/fhir/esavi/CodeSystem/SistemasDeCodificacionCS", "2", "WHODrug" ) );


        return item;
    }


    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataNombreNormalizadoVacuna(
            EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "nombreNormalizadoVacuna" ) );

        item.addAnswer()
                .setValue( new StringType( ctx.option(OPTIONSET_WHODRUG_COVID, ctx.dataElement( id ), id ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationManufacturerName(
        EsaviContext ctx, String manufacturerName )
    {
        if ( !ctx.hasDataElement( manufacturerName ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreFabricante" ) );

        item.addAnswer()
            .setValue( new StringType( ctx.dataElement( manufacturerName ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent codigoVacunaWHODrug(
            EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "codigoVacunaWHODrug" ) );

        String display = ctx.option(OPTIONSET_WHODRUG_COVID, ctx.dataElement( id ) );
        item.addAnswer().setValue(EsaviWhoDrug.get(ctx.dataElement( id ), display));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationIdentifier(
        EsaviContext ctx, String id )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "identificadorVacuna" ) );

        switch ( id )
        {
            case VACCINE_1:
                item.addAnswer().setValue( new IntegerType( "1" ) );
                return item;
            case VACCINE_2:
                item.addAnswer().setValue( new IntegerType( "2" ) );
                return item;
            case VACCINE_3:
                item.addAnswer().setValue( new IntegerType( "3" ) );
                return item;
            case VACCINE_4:
                item.addAnswer().setValue( new IntegerType( "4" ) );
                return item;
            default:
                return null;
        }
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

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationDiluentName(
            EsaviContext ctx, String diluentName )
    {
        if ( !ctx.hasDataElement( diluentName ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "nombreDiluyenteVacuna" ) );

        String display = ctx.option(OPTIONSET_DILUENTS, ctx.dataElement( diluentName ) );
        item.addAnswer().setValue( new StringType( display ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationDiluentBatch(
            EsaviContext ctx, String batch )
    {
        if ( !ctx.hasDataElement( batch ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "numeroLoteDiluyente" ) );

        item.addAnswer().setValue( new StringType( ctx.dataElement( batch ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataAdministrationDiluentExpiryDate(
            EsaviContext ctx,
            String expiryDate )
    {
        if ( !ctx.hasDataElement( expiryDate ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "fechaVencimientoDiluyente" ) );

        item.addAnswer().setValue( new DateType( ctx.dataElement( expiryDate ) ) );

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

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataVaccinationSite( EsaviContext ctx )
    {
        final String LUGAR_VACUNACION = "AIbRRSIHSqY";

        if ( !ctx.hasDataElement( LUGAR_VACUNACION ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "nombreVacunatorio" ) );

        item.addAnswer().setValue( new StringType( ctx.option(OPTIONSET_LUGAR_VACUNACION, ctx.dataElement( LUGAR_VACUNACION ) ) ) );

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

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataCodigoMecanismoVerificacion( EsaviContext ctx, String vaccine_id)
    {

        final String VACUNA_MECANISMO_VERIFICACION = "QvLFXpsCWAd";

        if (!vaccine_id.equals(VACCINE_1) || !ctx.hasDataElement( VACUNA_MECANISMO_VERIFICACION )){
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "codigoMecanismoVerificacion" ) );

        item.addAnswer().setValue(EsaviMecanismoVerificacionVacuna.get(ctx.dataElement(VACUNA_MECANISMO_VERIFICACION)));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataNombreOtroMecanismoVerificacion( EsaviContext ctx, String vaccine_id)
    {

        final String VACUNA_OTRO_MECANISMO_VERIFICACION = "F1sQvGLtfEw";
        if (!vaccine_id.equals(VACCINE_1) || !ctx.hasDataElement( VACUNA_OTRO_MECANISMO_VERIFICACION ))
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "nombreOtroMecanismoVerificacion" ) );

        item.addAnswer().setValue( new StringType(ctx.dataElement(VACUNA_OTRO_MECANISMO_VERIFICACION)));

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

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataReconstitutionDate(
            EsaviContext ctx,
            String reconstitutionDate )
    {
        if ( !ctx.hasDataElement( reconstitutionDate ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "fechaReconstitucionVacuna" ) );

        item.addAnswer().setValue( new DateType( ctx.dataElement( reconstitutionDate ) ) );

        return item;
    }

    // ---------------------------------------------------------------------------------
    // Esavi Registrations
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent vaccineDataReconstitutionTime(
            EsaviContext ctx,
            String reconstitutionTime )
    {
        if ( !ctx.hasDataElement( reconstitutionTime ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "horaReconstitucionVacuna" ) );

        String localTime = LocalTime.parse( ctx.dataElement( reconstitutionTime ) ).format( DateTimeFormatter.ISO_TIME );

        item.addAnswer().setValue( new TimeType( localTime));

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviRegistration(
        EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "registroESAVI" ) );

        // datosESAVI
        item.addItem( esaviData( ctx, "PZxZirhNzgS", "1", "LYariSd5cEq", "mqCTfs4jXSo", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "maY0Vi68Fv9", "2", "hfdzpv7lP6C", "hc15z2mXm2o", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "Sy1uqYvgR3r", "3", "oHVQ23x5NQE", "DBV8wfaQCMt", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "Og99AH5tIQz", "4", "OGRWlduylFk", "NuwfTxCxvca", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "vqf60JfNqsf", "5", "QeXeXYdBAUE", "XQjZ1N8dNkt", "ci3S3BH6wZn" ) );
        item.addItem( esaviData( ctx, "pQJc4VA2SDW", "6", "MfgJjmoOdxm", "kDgoKxw8sVJ", "ci3S3BH6wZn" ) );


        // ESAVIDuranteEmbarazo
        item.addItem( esaviDuringPregnancy (ctx));

        // gravedadESAVI
        item.addItem( esaviSeriousness( ctx ) );

        // desenlaceESAVI
        item.addItem( esaviOutcome( ctx ) );

        // causalidadESAVI
        item.addItem( causalidadESAVI( ctx, CAUSALITY_A1 ) );
        item.addItem( causalidadESAVI( ctx, CAUSALITY_A2 ) );
        item.addItem( causalidadESAVI( ctx, CAUSALITY_A3 ) );
        item.addItem( causalidadESAVI( ctx, CAUSALITY_A4 ) );
        item.addItem( causalidadESAVI( ctx, CAUSALITY_B1 ) );
        item.addItem( causalidadESAVI( ctx, CAUSALITY_B2 ) );
        item.addItem( causalidadESAVI( ctx, CAUSALITY_C ) );
        item.addItem( causalidadESAVI( ctx, CAUSALITY_D ) );

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

        // nombreESAVI
        item.addItem( esaviName( ctx, id ) );

        // IdentificadorESAVI
        item.addItem( esaviPosition( ctx, position ) );

        // codigoESAVIMedDRA
        item.addItem( esaviMeddraCode( ctx, id ) );

        // codigoESAVIOtro
        // No mapping

        // fechaESAVI
        item.addItem( esaviStartDate( ctx, startDate ) );

        // horaESAVI
        item.addItem( esaviStartTime( ctx, startTime ) );

        // descripcionESAVI
        item.addItem( esaviDescription( ctx, description ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviName( EsaviContext ctx, String id )
    {
        if ( !ctx.hasDataElement( id ) || !ctx.hasOption(OPTIONSET_MEDDRA, ctx.dataElement( id ) ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "nombreESAVI" ) );

        item.addAnswer().setValue( new StringType( ctx.option(OPTIONSET_MEDDRA, ctx.dataElement( id ) ) ) );

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

        String display = ctx.option(OPTIONSET_MEDDRA, ctx.dataElement( id ), "" );

        item.addAnswer().setValue(EsaviMeddra.get(ctx.dataElement( id ), display));

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

    // ---------------------------------------------------------------------------------
    // ESAVI Seriousness
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviDescription( EsaviContext ctx,
        String description )
    {
        if ( !ctx.hasDataElement( description ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "descripcionESAVI" ) );

        item.addAnswer()
            .setValue( new StringType( ctx.dataElement( description ) ) );

        return item;
    }

    // ---------------------------------------------------------------------------------
    // ESAVI Seriousness
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviDuringPregnancy(
            EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "ESAVIDuranteEmbarazo" ) );

        // codigoTipoComplicacionESAVI
        // TODO no mapping yet

        String DESCRIPTION_COMPLICATIONS_PREGNACY = "M8T2c8RJFUA";
        if ( ctx.hasDataElement( DESCRIPTION_COMPLICATIONS_PREGNACY ) )
        {
            QuestionnaireResponse.QuestionnaireResponseItemComponent internal_item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                    new StringType( "nombreComplicacionEmbarazoESAVI" ) );
            internal_item.addAnswer().setValue( new StringType( ctx.dataElement( DESCRIPTION_COMPLICATIONS_PREGNACY ) ) );
            item.addItem(internal_item);
        }

        if (! item.hasItem())
        {
            return null;
        }

        return item;
    }

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
        if ( !ctx.dataElementIsTrue( "IEOkkWbZwB0" ) )
        {
            return null;
        }

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "gravMuerteFetal" ) );

        item.addAnswer()
            .setValue( new BooleanType( ctx.dataElementAsBoolean( "IEOkkWbZwB0" ) ) );

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

    // ---------------------------------------------------------------------------------
    // ESAVI Outcome
    // ---------------------------------------------------------------------------------

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

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviOutcome( EsaviContext ctx )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
            new StringType( "desenlaceESAVI" ) );

        // codDesenlaceESAVI
        item.addItem( esaviOutcomeCode( ctx ) );

        // fechaMuerte
        item.addItem( fechaMuerte( ctx ) );

        // autopsia
        item.addItem( autopsia( ctx ) );

        // fechaNotificaMuerte
        // no mapping

        // fechaNotificaMuerteFetal
        // no mapping

        // autopsiaFetal
        // no mapping

        // comentarios
        //item.addItem( esaviOutcome_comentarios( ctx ) );

        // fechaInicioInvestigacion
        item.addItem( esaviOutcome_fechaInicioInvestigacion( ctx ) );

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
    // causalidadESAVI
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent causalidadESAVI( EsaviContext ctx, String id )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "causalidadESAVI" ) );

        // fechaCausalidadESAVI
        // no mapping

        // otroSistemaclasfcausalidad
        // no mapping

        // clasificacioncausaESAVI
        // no mapping

        // clasificacionDeCausalidadWHOAEFI
        item.addItem( clasificacionDeCausalidadWHOAEFI(ctx, id) );

        // clasificacionDeCausalidadWHOUMC
        // no mapping

        // clasificacionDeCausalidadNaranjo
        // no mapping

        // referenciaIdentificadorVacuna
        // no mapping

        // referenciaIdentificadorESAVI
        // no mapping

        // embarazoDuranteESAVI
        // no mapping

        if (!item.hasItem()) {
            return null;
        }

        // Only if there is a causality, the system is added
        // sistemaClasfcausalidad
        item.addItem( sistemaClasfcausalidad() );


        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent sistemaClasfcausalidad()
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "sistemaClasfcausalidad" ) );

        item.addAnswer()
                .setValue(
                        new Coding( "https://paho.org/fhir/esavi/CodeSystem/SistemaClasfCausalidadCS", "WHO-AEFI", "WHO-AEFI" ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent clasificacionDeCausalidadWHOAEFI(EsaviContext ctx, String id )
    {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "clasificacionDeCausalidadWHOAEFI" ) );

        switch ( id )
        {
            case CAUSALITY_A1:
                if (ctx.dataElementIsTrue(CAUSALITY_A1)){
                    item.addAnswer().setValue(new Coding( "https://paho.org/fhir/esavi/CodeSystem/ClasificacionDesenlaceWHOAEFICS", "A1", "Evento relacionado con la vacuna o cualquiera de sus componentes" ) );
                }
                break;
            case CAUSALITY_A2:
                if (ctx.dataElementIsTrue(CAUSALITY_A2)) {
                    item.addAnswer().setValue(new Coding("https://paho.org/fhir/esavi/CodeSystem/ClasificacionDesenlaceWHOAEFICS", "A2", "Evento relacionado con una desviacion de calidad del producto biologico o la vacuna"));
                }
                break;
            case CAUSALITY_A3:
                if (ctx.dataElementIsTrue(CAUSALITY_A3)) {
                    item.addAnswer().setValue(new Coding("https://paho.org/fhir/esavi/CodeSystem/ClasificacionDesenlaceWHOAEFICS", "A3", "Evento relacionado con un error programatico"));
                }
                break;
            case CAUSALITY_A4:
                if (ctx.dataElementIsTrue(CAUSALITY_A4)) {
                    item.addAnswer().setValue(new Coding("https://paho.org/fhir/esavi/CodeSystem/ClasificacionDesenlaceWHOAEFICS", "A4", "Evento por estres que tuvo lugar inmediatemente antes, durante o inmediatamente despues del proceso de vacunacion"));
                }
                break;
            case CAUSALITY_B1:
                if (ctx.dataElementIsTrue(CAUSALITY_B1)) {
                    item.addAnswer().setValue(new Coding("https://paho.org/fhir/esavi/CodeSystem/ClasificacionDesenlaceWHOAEFICS", "B1", "La relacion temporal es congruente, pero no hay evidencia definitiva suficiente sobre una relacion causal con la vacuna (puede ser un evento recientemente asociadoa la vacuna [señal])"));
                }
                break;
            case CAUSALITY_B2:
                if (ctx.dataElementIsTrue(CAUSALITY_B2)) {
                    item.addAnswer().setValue(new Coding("https://paho.org/fhir/esavi/CodeSystem/ClasificacionDesenlaceWHOAEFICS", "B2", "Factores determinantes para la clasificación muestran tendencias conflictivas a favor y en contra de una asociacion causal con la vacunació"));
                }
                break;
            case CAUSALITY_C:
                if (ctx.dataElementIsTrue(CAUSALITY_C)) {
                    item.addAnswer().setValue(new Coding("https://paho.org/fhir/esavi/CodeSystem/ClasificacionDesenlaceWHOAEFICS", "C", "Causa Coincidente"));
                }
                break;
            case CAUSALITY_D:
                if (ctx.dataElementIsTrue(CAUSALITY_D)) {
                    item.addAnswer().setValue(new Coding("https://paho.org/fhir/esavi/CodeSystem/ClasificacionDesenlaceWHOAEFICS", "NC", "No clasificable"));
                }
                break;
            default:
                return null;
        }

        if (!item.hasAnswer()) {
            return null;
        }
        return item;

    }

    // ---------------------------------------------------------------------------------
    // Common
    // ---------------------------------------------------------------------------------

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent fechaMuerte( EsaviContext ctx )
    {
        String DE_FECHA_MUERTE = "TKikUtqJQTq";
        if ( !ctx.hasDataElement( DE_FECHA_MUERTE ) )
        {
            return null;
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "fechaMuerte" ) );

        item.addAnswer().setValue( new DateType( ctx.dataElement( DE_FECHA_MUERTE ) ) );

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent autopsia( EsaviContext ctx )
    {
        // Escenario 1: ¿se solicitó autopsia?=si & ¿se solicitó autopsia verbal?=si, entonces autopsia=si.
        // Escenario 2: ¿se solicitó autopsia?=si & ¿se solicitó autopsia verbal?=no/vacio. En ese caso autopsia=si
        // Escenario 3: ¿se solicitó autopsia?=no/vacio & ¿se solicitó autopsia verbal?=si. En ese caso autopsia=si.
        // Escenario 4: ¿se solicitó autopsia?=no & ¿se solicitó autopsia verbal?=no/vacio. En ese caso autopsia=no.
        // Escenario 5: ¿se solicitó autopsia?=no/vacio & ¿se solicitó autopsia verbal?=no. En ese caso autopsia=no.
        // Escenario 6: ¿se solicitó autopsia?=vacio Y ¿se solicitó autopsia verbal?=vacío. En ese caso autopsia= No sabe

        String DE_AUTOPSIA = "YUcJrLWmGyv";
        String DE_AUTOPSIA_VERBAL = "CYZNXLLeOr6";

        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "autopsia" ) );

        boolean autopsia = false;
        boolean autopsia_verbal = false;

        if ( !ctx.hasDataElement( DE_AUTOPSIA ) && !ctx.hasDataElement( DE_AUTOPSIA_VERBAL ))
        {
            item.addAnswer().setValue(EsaviRespuestaSimple.get("3")); // No sabe
            return item;
        }

        if (ctx.hasDataElement( DE_AUTOPSIA )) {
            autopsia = Boolean.parseBoolean(ctx.dataElement( DE_AUTOPSIA ));
        }
        if (ctx.hasDataElement( DE_AUTOPSIA_VERBAL )) {
            autopsia_verbal = Boolean.parseBoolean(ctx.dataElement( DE_AUTOPSIA_VERBAL ));
        }

        if (autopsia || autopsia_verbal){
            item.addAnswer().setValue(EsaviRespuestaSimple.get("1")); // Si
        }
        else{
            item.addAnswer().setValue(EsaviRespuestaSimple.get("2")); // No
        }

        return item;
    }

    private static QuestionnaireResponse.QuestionnaireResponseItemComponent esaviOutcome_fechaInicioInvestigacion( EsaviContext ctx )
    {
        String DE_FECHA_INVESTIGACION = "e8ltdHdx90O";
        if ( !ctx.hasDataElement( DE_FECHA_INVESTIGACION ) )
        {
            return null;
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent(
                new StringType( "fechaInicioInvestigacion" ) );

        item.addAnswer().setValue( new DateType( ctx.dataElement( DE_FECHA_INVESTIGACION ) ) );

        return item;
    }



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
}
