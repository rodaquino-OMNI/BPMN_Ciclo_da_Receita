# Form Enhancements - SUB_02 Pre-Atendimento

## Forms Enhanced: 2

### 1. Task_ColetarSinaisVitais (Enhanced)

**Original Fields:**
- pressaoArterial (string)
- frequenciaCardiaca (long)
- temperatura (double)
- saturacaoO2 (long)
- glicemia (long)

**New Fields Added:**

```xml
<camunda:formField id="dataHoraColeta"
                    label="Data/Hora da Coleta"
                    type="date"
                    defaultValue="${now()}" />

<camunda:formField id="responsavelColeta"
                    label="Responsável pela Coleta"
                    type="enum">
  <camunda:value id="enfermeiro" name="Enfermeiro(a)" />
  <camunda:value id="tecnico" name="Técnico(a) de Enfermagem" />
  <camunda:value id="medico" name="Médico(a)" />
</camunda:formField>
```

**Complete Enhanced Form:**

```xml
<bpmn:userTask id="Task_ColetarSinaisVitais" name="Coletar Sinais Vitais" camunda:assignee="${enfermeiro}">
  <bpmn:extensionElements>
    <camunda:formData>
      <camunda:formField id="dataHoraColeta"
                          label="Data/Hora da Coleta"
                          type="date"
                          defaultValue="${now()}" />
      <camunda:formField id="pressaoArterial" label="Pressão Arterial" type="string" />
      <camunda:formField id="frequenciaCardiaca" label="Frequência Cardíaca" type="long" />
      <camunda:formField id="temperatura" label="Temperatura (°C)" type="double" />
      <camunda:formField id="saturacaoO2" label="Saturação O2 (%)" type="long" />
      <camunda:formField id="glicemia" label="Glicemia" type="long" />
      <camunda:formField id="responsavelColeta"
                          label="Responsável pela Coleta"
                          type="enum">
        <camunda:value id="enfermeiro" name="Enfermeiro(a)" />
        <camunda:value id="tecnico" name="Técnico(a) de Enfermagem" />
        <camunda:value id="medico" name="Médico(a)" />
      </camunda:formField>
    </camunda:formData>
  </bpmn:extensionElements>
  <bpmn:incoming>Flow_Triagem_To_Sinais</bpmn:incoming>
  <bpmn:outgoing>Flow_Sinais_To_Gateway</bpmn:outgoing>
</bpmn:userTask>
```

### 2. Task_RealizarTriagem (Enhanced)

**Original Fields:**
- queixaPrincipal (string)
- historico (string)
- alergias (string)
- medicamentosUso (string)

**New Fields Added:**

```xml
<camunda:formField id="classificacaoRisco"
                    label="Classificação de Risco"
                    type="enum">
  <camunda:value id="vermelha" name="Vermelha - Emergência" />
  <camunda:value id="laranja" name="Laranja - Muito Urgente" />
  <camunda:value id="amarela" name="Amarela - Urgente" />
  <camunda:value id="verde" name="Verde - Pouco Urgente" />
  <camunda:value id="azul" name="Azul - Não Urgente" />
</camunda:formField>

<camunda:formField id="dataTriagem"
                    label="Data da Triagem"
                    type="date"
                    defaultValue="${now()}" />
```

**Complete Enhanced Form:**

```xml
<bpmn:userTask id="Task_RealizarTriagem" name="Realizar Triagem" camunda:assignee="${enfermeiro}">
  <bpmn:extensionElements>
    <camunda:formData>
      <camunda:formField id="dataTriagem"
                          label="Data da Triagem"
                          type="date"
                          defaultValue="${now()}" />
      <camunda:formField id="queixaPrincipal" label="Queixa Principal" type="string" />
      <camunda:formField id="historico" label="Histórico Clínico" type="string" />
      <camunda:formField id="alergias" label="Alergias" type="string" />
      <camunda:formField id="medicamentosUso" label="Medicamentos em Uso" type="string" />
      <camunda:formField id="classificacaoRisco"
                          label="Classificação de Risco"
                          type="enum">
        <camunda:value id="vermelha" name="Vermelha - Emergência" />
        <camunda:value id="laranja" name="Laranja - Muito Urgente" />
        <camunda:value id="amarela" name="Amarela - Urgente" />
        <camunda:value id="verde" name="Verde - Pouco Urgente" />
        <camunda:value id="azul" name="Azul - Não Urgente" />
      </camunda:formField>
    </camunda:formData>
  </bpmn:extensionElements>
  <bpmn:incoming>Flow_Start_To_Triagem</bpmn:incoming>
  <bpmn:outgoing>Flow_Triagem_To_Sinais</bpmn:outgoing>
</bpmn:userTask>
```

## Summary
- **Forms Enhanced:** 2
- **Date Fields Added:** 2
- **Enum Fields Added:** 2
- **Total New Fields:** 4
