# Form Enhancements - SUB_07 Denials Management

## Forms Enhanced: 1

### Task_Human_Review_Appeal (Enhanced)

**Original Fields:**
- appealDocument (string)
- approved (boolean)
- reviewComments (string)

**New Fields Added:**

```xml
<camunda:formField id="prazoRecurso"
                    label="Prazo Limite para Recurso"
                    type="date" />

<camunda:formField id="probabilidadeSuccesso"
                    label="Probabilidade de Sucesso"
                    type="enum">
  <camunda:value id="baixa" name="Baixa (0-30%)" />
  <camunda:value id="media" name="Média (30-70%)" />
  <camunda:value id="alta" name="Alta (70-100%)" />
</camunda:formField>

<camunda:formField id="complexidadeRecurso"
                    label="Complexidade do Recurso"
                    type="enum">
  <camunda:value id="simples" name="Simples" />
  <camunda:value id="moderada" name="Moderada" />
  <camunda:value id="complexa" name="Complexa" />
  <camunda:value id="muito_complexa" name="Muito Complexa" />
</camunda:formField>
```

**Complete Enhanced Form:**

```xml
<bpmn:userTask id="Task_Human_Review_Appeal" name="Revisar Recurso" camunda:assignee="${analista_glosas}">
  <bpmn:extensionElements>
    <camunda:formData>
      <camunda:formField id="appealDocument" label="Documento de Recurso" type="string" />
      <camunda:formField id="prazoRecurso"
                          label="Prazo Limite para Recurso"
                          type="date" />
      <camunda:formField id="probabilidadeSuccesso"
                          label="Probabilidade de Sucesso"
                          type="enum">
        <camunda:value id="baixa" name="Baixa (0-30%)" />
        <camunda:value id="media" name="Média (30-70%)" />
        <camunda:value id="alta" name="Alta (70-100%)" />
      </camunda:formField>
      <camunda:formField id="complexidadeRecurso"
                          label="Complexidade do Recurso"
                          type="enum">
        <camunda:value id="simples" name="Simples" />
        <camunda:value id="moderada" name="Moderada" />
        <camunda:value id="complexa" name="Complexa" />
        <camunda:value id="muito_complexa" name="Muito Complexa" />
      </camunda:formField>
      <camunda:formField id="approved" label="Aprovar Recurso?" type="boolean" />
      <camunda:formField id="reviewComments" label="Comentários" type="string" />
    </camunda:formData>
  </bpmn:extensionElements>
  <bpmn:incoming>Flow_Generate_To_Review</bpmn:incoming>
  <bpmn:outgoing>Flow_Review_To_Submit</bpmn:outgoing>
</bpmn:userTask>
```

## Summary
- **Forms Enhanced:** 1
- **Date Fields Added:** 1
- **Enum Fields Added:** 2
- **Total New Fields:** 3
