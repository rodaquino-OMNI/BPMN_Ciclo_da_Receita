# Form Enhancements - SUB_06 Billing Submission

## Forms Enhanced: 1

### Task_Fix_Errors (Enhanced)

**Original Fields:**
- validationErrors (string)
- corrections (string)

**New Fields Added:**

```xml
<camunda:formField id="severidadeErro"
                    label="Severidade do Erro"
                    type="enum">
  <camunda:value id="baixa" name="Baixa" />
  <camunda:value id="media" name="Média" />
  <camunda:value id="alta" name="Alta" />
  <camunda:value id="critica" name="Crítica" />
</camunda:formField>

<camunda:formField id="prazoCorrecao"
                    label="Prazo para Correção"
                    type="date" />

<camunda:formField id="tipoErro"
                    label="Tipo de Erro"
                    type="enum">
  <camunda:value id="tecnico" name="Técnico - Formato/Validação" />
  <camunda:value id="dados" name="Dados Incompletos" />
  <camunda:value id="contratual" name="Regra Contratual" />
  <camunda:value id="tiss" name="Padrão TISS" />
  <camunda:value id="outro" name="Outro" />
</camunda:formField>
```

**Complete Enhanced Form:**

```xml
<bpmn:userTask id="Task_Fix_Errors" name="Corrigir Erros" camunda:assignee="${faturista}">
  <bpmn:extensionElements>
    <camunda:formData>
      <camunda:formField id="validationErrors" label="Erros Encontrados" type="string" />
      <camunda:formField id="severidadeErro"
                          label="Severidade do Erro"
                          type="enum">
        <camunda:value id="baixa" name="Baixa" />
        <camunda:value id="media" name="Média" />
        <camunda:value id="alta" name="Alta" />
        <camunda:value id="critica" name="Crítica" />
      </camunda:formField>
      <camunda:formField id="tipoErro"
                          label="Tipo de Erro"
                          type="enum">
        <camunda:value id="tecnico" name="Técnico - Formato/Validação" />
        <camunda:value id="dados" name="Dados Incompletos" />
        <camunda:value id="contratual" name="Regra Contratual" />
        <camunda:value id="tiss" name="Padrão TISS" />
        <camunda:value id="outro" name="Outro" />
      </camunda:formField>
      <camunda:formField id="prazoCorrecao"
                          label="Prazo para Correção"
                          type="date" />
      <camunda:formField id="corrections" label="Correções Aplicadas" type="string" />
    </camunda:formData>
  </bpmn:extensionElements>
  <bpmn:incoming>Flow_Valid_No</bpmn:incoming>
  <bpmn:outgoing>Flow_Fix_To_Validation</bpmn:outgoing>
</bpmn:userTask>
```

## Summary
- **Forms Enhanced:** 1
- **Date Fields Added:** 1
- **Enum Fields Added:** 2
- **Total New Fields:** 3
