# BPMN Visual Layout Standards - Hospital Revenue Cycle

## Purpose

This document defines mandatory visual layout standards for all BPMN diagrams to ensure consistency, readability, and proper rendering in Camunda Modeler.

**Version**: 1.0.0
**Last Updated**: 2025-12-08
**Status**: MANDATORY

---

## 1. COORDINATE SYSTEM

### 1.1 Origin and Direction

```
(0,0) ────────────► X-axis (horizontal)
  │
  │
  │
  │
  ▼
Y-axis (vertical)
```

**Rules**:
- Origin at top-left corner
- X increases left-to-right
- Y increases top-to-bottom
- All coordinates in pixels
- Use integer values only

---

## 2. ELEMENT DIMENSIONS

### 2.1 Standard Dimensions (in pixels)

| Element Type | Width | Height | Notes |
|-------------|-------|--------|-------|
| **Tasks** | | | |
| Service Task | 100 | 80 | Standard task size |
| User Task | 100 | 80 | Same as service task |
| Manual Task | 100 | 80 | Same as service task |
| Script Task | 100 | 80 | Same as service task |
| Business Rule Task | 100 | 80 | Same as service task |
| Send Task | 100 | 80 | Same as service task |
| Receive Task | 100 | 80 | Same as service task |
| **Gateways** | | | |
| Exclusive Gateway | 50 | 50 | Diamond shape |
| Parallel Gateway | 50 | 50 | Diamond shape |
| Inclusive Gateway | 50 | 50 | Diamond shape |
| Event-Based Gateway | 50 | 50 | Diamond shape |
| **Events** | | | |
| Start Event | 36 | 36 | Circle |
| End Event | 36 | 36 | Circle |
| Intermediate Event | 36 | 36 | Circle |
| Boundary Event | 36 | 36 | Circle |
| **Subprocesses** | | | |
| Call Activity (collapsed) | 100 | 80 | Plus sign indicator |
| Subprocess (collapsed) | 100 | 80 | Plus sign indicator |
| Subprocess (expanded) | 350-600 | 200-400 | Variable based on content |
| **Pools & Lanes** | | | |
| Participant (Pool) | 2000-6000 | Variable | Based on lanes |
| Lane | Pool width | 150-250 | Per lane content |
| Lane Header | 30 | Lane height | Name area |
| **Annotations** | | | |
| Text Annotation | 100-200 | 30-50 | Variable width |

### 2.2 XML Dimension Format

```xml
<!-- Task -->
<bpmndi:BPMNShape id="Task_Example_di" bpmnElement="Task_Example">
  <dc:Bounds x="300" y="140" width="100" height="80" />
</bpmndi:BPMNShape>

<!-- Gateway -->
<bpmndi:BPMNShape id="Gateway_Example_di" bpmnElement="Gateway_Example">
  <dc:Bounds x="475" y="155" width="50" height="50" />
</bpmndi:BPMNShape>

<!-- Start Event -->
<bpmndi:BPMNShape id="Event_Start_di" bpmnElement="Event_Start">
  <dc:Bounds x="182" y="162" width="36" height="36" />
</bpmndi:BPMNShape>
```

---

## 3. SPACING & ALIGNMENT

### 3.1 Horizontal Spacing

```
Task1    ←─ 150-200px ─→    Task2    ←─ 150-200px ─→    Task3
[100x80]                    [100x80]                    [100x80]
```

**Rules**:
- **Standard spacing**: 150-200px between task centers
- **Minimum spacing**: 120px (to avoid overlap)
- **Gateway spacing**: 75-100px from adjacent tasks
- **Complex flows**: Up to 250px for clarity

### 3.2 Vertical Spacing

```
Lane 1 (200px height)
  ├─ Task centered vertically

Lane 2 (200px height)
  ├─ Task centered vertically

Lane 3 (200px height)
  ├─ Task centered vertically
```

**Rules**:
- **Lane height**: 150-250px depending on content
- **Vertical task alignment**: Center of lane = lane_y + (lane_height / 2)
- **Multi-lane spacing**: No gaps between lanes

### 3.3 Pool/Participant Layout

```
┌─────────────────────────────────────────────────┐
│ Pool Header (30px)        Pool Name             │
├─────────────────────────────────────────────────┤
│ Lane 1                                          │
│ Height: 200px                                    │
├─────────────────────────────────────────────────┤
│ Lane 2                                          │
│ Height: 200px                                    │
├─────────────────────────────────────────────────┤
│ Lane 3                                          │
│ Height: 200px                                    │
└─────────────────────────────────────────────────┘
```

**Participant Dimensions**:
```xml
<bpmndi:BPMNShape id="Participant_Hospital_di" bpmnElement="Participant_Hospital" isHorizontal="true">
  <dc:Bounds x="160" y="80" width="2000" height="600" />
</bpmndi:BPMNShape>
```

**Lane Dimensions** (stacked inside participant):
```xml
<!-- Lane 1 -->
<bpmndi:BPMNShape id="Lane_01_di" bpmnElement="Lane_01" isHorizontal="true">
  <dc:Bounds x="190" y="80" width="1970" height="200" />
</bpmndi:BPMNShape>

<!-- Lane 2 (starts at Lane1_y + Lane1_height) -->
<bpmndi:BPMNShape id="Lane_02_di" bpmnElement="Lane_02" isHorizontal="true">
  <dc:Bounds x="190" y="280" width="1970" height="200" />
</bpmndi:BPMNShape>

<!-- Lane 3 -->
<bpmndi:BPMNShape id="Lane_03_di" bpmnElement="Lane_03" isHorizontal="true">
  <dc:Bounds x="190" y="480" width="1970" height="200" />
</bpmndi:BPMNShape>
```

**Calculation**:
- Pool X: 160 (standard left margin)
- Pool Y: 80 (standard top margin)
- Lane X: Pool X + 30 (lane header width)
- Lane Y: Cumulative height of previous lanes
- Lane Width: Pool Width - 30

---

## 4. ELEMENT POSITIONING

### 4.1 Task Centering in Lane

```
Lane Y: 80, Lane Height: 200
Task Height: 80

Task Y = Lane Y + (Lane Height / 2) - (Task Height / 2)
Task Y = 80 + (200 / 2) - (80 / 2)
Task Y = 80 + 100 - 40
Task Y = 140 ✓
```

**Formula**:
```
task_y = lane_y + (lane_height - task_height) / 2
```

### 4.2 Gateway Centering

```
Lane Y: 80, Lane Height: 200
Gateway Height: 50

Gateway Y = Lane Y + (Lane Height / 2) - (Gateway Height / 2)
Gateway Y = 80 + (200 / 2) - (50 / 2)
Gateway Y = 80 + 100 - 25
Gateway Y = 155 ✓
```

### 4.3 Event Centering

```
Lane Y: 80, Lane Height: 200
Event Height: 36

Event Y = Lane Y + (Lane Height / 2) - (Event Height / 2)
Event Y = 80 + 100 - 18
Event Y = 162 ✓
```

### 4.4 Standard Flow Layout (Single Lane)

```xml
<!-- Pool -->
<bpmndi:BPMNShape id="Participant_Hospital_di" bpmnElement="Participant_Hospital" isHorizontal="true">
  <dc:Bounds x="160" y="80" width="2000" height="200" />
</bpmndi:BPMNShape>

<!-- Lane -->
<bpmndi:BPMNShape id="Lane_Example_di" bpmnElement="Lane_Example" isHorizontal="true">
  <dc:Bounds x="190" y="80" width="1970" height="200" />
</bpmndi:BPMNShape>

<!-- Start Event (x=230) -->
<bpmndi:BPMNShape id="Event_Start_di" bpmnElement="Event_Start">
  <dc:Bounds x="230" y="162" width="36" height="36" />
</bpmndi:BPMNShape>

<!-- Task 1 (x=330, spacing 100px from start) -->
<bpmndi:BPMNShape id="Task_1_di" bpmnElement="Task_1">
  <dc:Bounds x="330" y="140" width="100" height="80" />
</bpmndi:BPMNShape>

<!-- Gateway (x=505, spacing 175px from Task 1) -->
<bpmndi:BPMNShape id="Gateway_1_di" bpmnElement="Gateway_1">
  <dc:Bounds x="505" y="155" width="50" height="50" />
</bpmndi:BPMNShape>

<!-- Task 2 (x=630, spacing 125px from Gateway) -->
<bpmndi:BPMNShape id="Task_2_di" bpmnElement="Task_2">
  <dc:Bounds x="630" y="140" width="100" height="80" />
</bpmndi:BPMNShape>

<!-- End Event (x=805, spacing 175px from Task 2) -->
<bpmndi:BPMNShape id="Event_End_di" bpmnElement="Event_End">
  <dc:Bounds x="805" y="162" width="36" height="36" />
</bpmndi:BPMNShape>
```

---

## 5. SEQUENCE FLOW WAYPOINTS

### 5.1 Direct Horizontal Connection

```
Task1 ────────────► Task2

Task1 bounds: x=300, y=140, w=100, h=80 (center: 350, 180)
Task2 bounds: x=500, y=140, w=100, h=80 (center: 550, 180)
```

```xml
<bpmndi:BPMNEdge id="Flow_1_to_2_di" bpmnElement="Flow_1_to_2">
  <di:waypoint x="400" y="180" /> <!-- Task1 right edge center -->
  <di:waypoint x="500" y="180" /> <!-- Task2 left edge center -->
</bpmndi:BPMNEdge>
```

### 5.2 Gateway Split (Horizontal)

```
              ┌─► Task2 (y=100)
              │
Task1 ───► Gateway
              │
              └─► Task3 (y=240)

Gateway: x=505, y=155, w=50, h=50 (center: 530, 180)
Task2: x=630, y=100, w=100, h=80 (center: 680, 140)
Task3: x=630, y=240, w=100, h=80 (center: 680, 280)
```

```xml
<!-- Gateway to Task2 (upper branch) -->
<bpmndi:BPMNEdge id="Flow_G_to_T2_di" bpmnElement="Flow_G_to_T2">
  <di:waypoint x="555" y="180" /> <!-- Gateway right edge -->
  <di:waypoint x="590" y="180" /> <!-- Horizontal -->
  <di:waypoint x="590" y="140" /> <!-- Vertical to Task2 level -->
  <di:waypoint x="630" y="140" /> <!-- Task2 left edge -->
</bpmndi:BPMNEdge>

<!-- Gateway to Task3 (lower branch) -->
<bpmndi:BPMNEdge id="Flow_G_to_T3_di" bpmnElement="Flow_G_to_T3">
  <di:waypoint x="555" y="180" /> <!-- Gateway right edge -->
  <di:waypoint x="590" y="180" /> <!-- Horizontal -->
  <di:waypoint x="590" y="280" /> <!-- Vertical to Task3 level -->
  <di:waypoint x="630" y="280" /> <!-- Task3 left edge -->
</bpmndi:BPMNEdge>
```

### 5.3 Cross-Lane Connection

```
Lane 1 (y=80-280):     Task1 (y=140)
                          │
                          ↓
Lane 2 (y=280-480):    Task2 (y=340)

Task1: x=300, y=140, w=100, h=80 (center: 350, 180)
Task2: x=300, y=340, w=100, h=80 (center: 350, 380)
```

```xml
<bpmndi:BPMNEdge id="Flow_L1_to_L2_di" bpmnElement="Flow_L1_to_L2">
  <di:waypoint x="350" y="220" /> <!-- Task1 bottom edge center -->
  <di:waypoint x="350" y="340" /> <!-- Task2 top edge center -->
</bpmndi:BPMNEdge>
```

### 5.4 Waypoint Best Practices

**Rules**:
1. **Minimize waypoints**: Use 2-4 waypoints typically
2. **Orthogonal routing**: Prefer horizontal/vertical lines
3. **Connection points**: Attach to element edge centers
4. **Avoid overlaps**: Route around other elements
5. **45° angles**: Avoid diagonal lines when possible

**Edge Attachment Points**:
```
Task (100x80 at x=300, y=140):

Top center:    (350, 140) = (x + w/2, y)
Right center:  (400, 180) = (x + w, y + h/2)
Bottom center: (350, 220) = (x + w/2, y + h)
Left center:   (300, 180) = (x, y + h/2)
```

---

## 6. MESSAGE FLOW LAYOUT

### 6.1 Pool-to-Pool Message Flow

```
Pool 1 (y=0-60):    [Patient]
                        │
                        ↓ Message
Pool 2 (y=80-680):  [Hospital]
                     Start Event
```

```xml
<!-- External Pool (Black Box) -->
<bpmndi:BPMNShape id="Participant_Patient_di" bpmnElement="Participant_Patient" isHorizontal="true">
  <dc:Bounds x="160" y="0" width="600" height="60" />
</bpmndi:BPMNShape>

<!-- Internal Pool -->
<bpmndi:BPMNShape id="Participant_Hospital_di" bpmnElement="Participant_Hospital" isHorizontal="true">
  <dc:Bounds x="160" y="80" width="2000" height="600" />
</bpmndi:BPMNShape>

<!-- Message Flow -->
<bpmndi:BPMNEdge id="MsgFlow_Patient_Hospital_di" bpmnElement="MsgFlow_Patient_Hospital">
  <di:waypoint x="460" y="60" />  <!-- Patient pool bottom center -->
  <di:waypoint x="460" y="162" /> <!-- Start event in Hospital pool -->
</bpmndi:BPMNEdge>
```

### 6.2 Task-to-External-Pool Message Flow

```xml
<!-- Message from Hospital task to Insurance pool -->
<bpmndi:BPMNEdge id="MsgFlow_Hospital_Insurance_di" bpmnElement="MsgFlow_Hospital_Insurance">
  <di:waypoint x="680" y="220" /> <!-- Task bottom edge -->
  <di:waypoint x="680" y="750" /> <!-- Insurance pool top edge -->
</bpmndi:BPMNEdge>
```

**Message Flow Styling**:
- Dashed line (automatic in Camunda Modeler)
- Small circle at source
- Open arrowhead at target

---

## 7. LABEL POSITIONING

### 7.1 Task Labels

**Internal Label** (preferred):
```xml
<bpmndi:BPMNShape id="Task_Example_di" bpmnElement="Task_Example">
  <dc:Bounds x="300" y="140" width="100" height="80" />
  <bpmndi:BPMNLabel /> <!-- Auto-centered inside task -->
</bpmndi:BPMNShape>
```

**External Label** (for long names):
```xml
<bpmndi:BPMNShape id="Task_Example_di" bpmnElement="Task_Example">
  <dc:Bounds x="300" y="140" width="100" height="80" />
  <bpmndi:BPMNLabel>
    <dc:Bounds x="305" y="185" width="90" height="40" />
  </bpmndi:BPMNLabel>
</bpmndi:BPMNShape>
```

### 7.2 Gateway Labels

**Above Gateway**:
```xml
<bpmndi:BPMNShape id="Gateway_Example_di" bpmnElement="Gateway_Example">
  <dc:Bounds x="505" y="155" width="50" height="50" />
  <bpmndi:BPMNLabel>
    <dc:Bounds x="498" y="125" width="64" height="27" />
  </bpmndi:BPMNLabel>
</bpmndi:BPMNShape>
```

**Below Gateway**:
```xml
<bpmndi:BPMNLabel>
  <dc:Bounds x="498" y="212" width="64" height="27" />
</bpmndi:BPMNLabel>
```

### 7.3 Flow Labels

**Condition Labels** (on sequence flows):
```xml
<bpmndi:BPMNEdge id="Flow_Yes_di" bpmnElement="Flow_Yes">
  <di:waypoint x="555" y="180" />
  <di:waypoint x="630" y="180" />
  <bpmndi:BPMNLabel>
    <dc:Bounds x="580" y="162" width="24" height="14" />
  </bpmndi:BPMNLabel>
</bpmndi:BPMNEdge>
```

**Best Practices**:
- Position label near mid-point of flow
- Offset slightly (±5-10px) to avoid line overlap
- Keep labels small (max 3-4 words)

### 7.4 Event Labels

**Below Event**:
```xml
<bpmndi:BPMNShape id="Event_Start_di" bpmnElement="Event_Start">
  <dc:Bounds x="230" y="162" width="36" height="36" />
  <bpmndi:BPMNLabel>
    <dc:Bounds x="220" y="205" width="56" height="27" />
  </bpmndi:BPMNLabel>
</bpmndi:BPMNShape>
```

---

## 8. LAYOUT TEMPLATES

### 8.1 Orchestrator Process Layout

```
Pool: Hospital (x=160, y=80, w=6000, h=2200)
  Lane 01 (y=80, h=220):  [CallActivity_SUB_01]
  Lane 02 (y=300, h=220): [Gateway] → [CallActivity_SUB_02]
  Lane 03 (y=520, h=220): [CallActivity_SUB_03]
  Lane 04 (y=740, h=220): [CallActivity_SUB_04]
  Lane 05 (y=960, h=220): [CallActivity_SUB_05]
  Lane 06 (y=1180, h=220): [CallActivity_SUB_06]
  Lane 07 (y=1400, h=220): [Gateway] → [CallActivity_SUB_07]
  Lane 08 (y=1620, h=220): [CallActivity_SUB_08]
  Lane 09 (y=1840, h=220): [ParallelGateway] → [CallActivity_SUB_09]
  Lane 10 (y=2060, h=220): [CallActivity_SUB_10] → [ParallelGateway]
```

**X-coordinates for linear flow**:
```
Start: x=230
CallActivity_SUB_01: x=350
CallActivity_SUB_02: x=550
CallActivity_SUB_03: x=750
...
End: x=5800
```

### 8.2 Subprocess Layout (3-4 Lanes)

```
Pool: Subprocess (x=160, y=80, w=2000, h=800)
  Lane 1 (y=80, h=200):  [Start] → [Task1] → [Task2]
  Lane 2 (y=280, h=200): [Task3] → [Gateway] → [Task4]
  Lane 3 (y=480, h=200): [Task5] → [Task6]
  Lane 4 (y=680, h=200): [Task7] → [End]
```

---

## 9. BOUNDARY EVENT POSITIONING

### 9.1 Bottom Edge (Timer)

```
Task (x=300, y=140, w=100, h=80)
Boundary Event (w=36, h=36)

Event X = Task X + (Task Width / 2) - (Event Width / 2)
Event X = 300 + 50 - 18 = 332

Event Y = Task Y + Task Height - (Event Height / 2)
Event Y = 140 + 80 - 18 = 202
```

```xml
<bpmndi:BPMNShape id="Event_Boundary_Timer_di" bpmnElement="Event_Boundary_Timer">
  <dc:Bounds x="332" y="202" width="36" height="36" />
</bpmndi:BPMNShape>
```

### 9.2 Right Edge (Error)

```
Event X = Task X + Task Width - (Event Width / 2)
Event X = 300 + 100 - 18 = 382

Event Y = Task Y + (Task Height / 2) - (Event Height / 2)
Event Y = 140 + 40 - 18 = 162
```

```xml
<bpmndi:BPMNShape id="Event_Boundary_Error_di" bpmnElement="Event_Boundary_Error">
  <dc:Bounds x="382" y="162" width="36" height="36" />
</bpmndi:BPMNShape>
```

---

## 10. TEXT ANNOTATIONS

### 10.1 Annotation Positioning

```xml
<!-- Annotation (info note) -->
<bpmndi:BPMNShape id="TextAnnotation_Info_di" bpmnElement="TextAnnotation_Info">
  <dc:Bounds x="450" y="50" width="150" height="40" />
</bpmndi:BPMNShape>

<!-- Association (dotted line to task) -->
<bpmndi:BPMNEdge id="Association_Info_to_Task_di" bpmnElement="Association_Info_to_Task">
  <di:waypoint x="525" y="90" />  <!-- Annotation bottom -->
  <di:waypoint x="400" y="140" /> <!-- Task top -->
</bpmndi:BPMNEdge>
```

**Best Practices**:
- Place above or to the side of element
- Use sparingly (only for critical notes)
- Keep text concise (max 2-3 lines)

---

## 11. VALIDATION CHECKLIST

### 11.1 Visual Quality Checks

- [ ] All elements visible (no negative coordinates)
- [ ] No overlapping elements
- [ ] Consistent spacing between elements
- [ ] Labels readable and not overlapping
- [ ] Flows have clear direction (left-to-right)
- [ ] Gateway branches clearly separated
- [ ] Lane heights accommodate all content
- [ ] Pool widths accommodate longest path
- [ ] Boundary events properly positioned
- [ ] Message flows cross pool boundaries correctly

### 11.2 Coordinate Validation

```python
# Pseudo-code for validation
def validate_task_in_lane(task, lane):
    task_top = task.y
    task_bottom = task.y + task.height
    lane_top = lane.y
    lane_bottom = lane.y + lane.height

    assert task_top >= lane_top, "Task extends above lane"
    assert task_bottom <= lane_bottom, "Task extends below lane"

def validate_spacing(element1, element2):
    distance_x = abs((element1.x + element1.width/2) - (element2.x + element2.width/2))
    assert distance_x >= 120, "Elements too close horizontally"
```

---

## 12. LAYOUT CALCULATION HELPERS

### 12.1 Python Helper Functions

```python
def calculate_task_center_y(lane_y, lane_height, task_height=80):
    """Calculate Y coordinate to center task in lane"""
    return lane_y + (lane_height - task_height) // 2

def calculate_gateway_center_y(lane_y, lane_height, gateway_height=50):
    """Calculate Y coordinate to center gateway in lane"""
    return lane_y + (lane_height - gateway_height) // 2

def calculate_event_center_y(lane_y, lane_height, event_height=36):
    """Calculate Y coordinate to center event in lane"""
    return lane_y + (lane_height - event_height) // 2

def generate_sequence_flow_waypoints(source, target):
    """Generate waypoints for horizontal sequence flow"""
    source_x = source['x'] + source['width']
    source_y = source['y'] + source['height'] // 2
    target_x = target['x']
    target_y = target['y'] + target['height'] // 2

    if source_y == target_y:
        # Direct horizontal connection
        return [(source_x, source_y), (target_x, target_y)]
    else:
        # Route with intermediate waypoint
        mid_x = (source_x + target_x) // 2
        return [
            (source_x, source_y),
            (mid_x, source_y),
            (mid_x, target_y),
            (target_x, target_y)
        ]
```

### 12.2 Example Usage

```python
# Define lane
lane = {'x': 190, 'y': 80, 'width': 1970, 'height': 200}

# Calculate task position
task1 = {
    'x': 330,
    'y': calculate_task_center_y(lane['y'], lane['height']),
    'width': 100,
    'height': 80
}
# Result: task1['y'] = 140

# Calculate gateway position
gateway1 = {
    'x': 505,
    'y': calculate_gateway_center_y(lane['y'], lane['height']),
    'width': 50,
    'height': 50
}
# Result: gateway1['y'] = 155

# Generate waypoints
waypoints = generate_sequence_flow_waypoints(task1, gateway1)
# Result: [(430, 180), (505, 180)]
```

---

## 13. CAMUNDA MODELER COMPATIBILITY

### 13.1 Auto-Layout Feature

Camunda Modeler provides auto-layout, but manual layout is preferred for:
- Precise control over visual appearance
- Consistency across multiple files
- Complex diagrams with many lanes
- Custom spacing requirements

### 13.2 Grid Snapping

**Settings**:
- Grid size: 10px (default in Camunda Modeler)
- Snap to grid: Enabled (recommended)
- All coordinates should align to 10px grid

**Example**:
```
✓ Good: x=330, y=140
✗ Bad:  x=333, y=142
```

---

## 14. EXPORT FORMATS

### 14.1 BPMN XML

Standard format, includes all layout information in `<bpmndi:BPMNDiagram>` section.

### 14.2 SVG Export

For documentation, use Camunda Modeler export:
- File → Export → SVG
- Resolution: 300 DPI
- Include: All elements

### 14.3 PNG Export

For presentations:
- File → Export → PNG
- Scale: 2x (for high-resolution)

---

## APPENDIX: COMPLETE EXAMPLE

### Single-Lane Process with All Element Types

```xml
<bpmndi:BPMNDiagram id="BPMNDiagram_Example">
  <bpmndi:BPMNPlane id="BPMNPlane_Example" bpmnElement="Collaboration_Example">

    <!-- Pool -->
    <bpmndi:BPMNShape id="Participant_Example_di" bpmnElement="Participant_Example" isHorizontal="true">
      <dc:Bounds x="160" y="80" width="1200" height="200" />
    </bpmndi:BPMNShape>

    <!-- Lane -->
    <bpmndi:BPMNShape id="Lane_Example_di" bpmnElement="Lane_Example" isHorizontal="true">
      <dc:Bounds x="190" y="80" width="1170" height="200" />
    </bpmndi:BPMNShape>

    <!-- Start Event (x=230, centered in lane) -->
    <bpmndi:BPMNShape id="Event_Start_di" bpmnElement="Event_Start">
      <dc:Bounds x="230" y="162" width="36" height="36" />
      <bpmndi:BPMNLabel>
        <dc:Bounds x="220" y="205" width="56" height="14" />
      </bpmndi:BPMNLabel>
    </bpmndi:BPMNShape>

    <!-- Task 1 (x=330, spacing 100px from start) -->
    <bpmndi:BPMNShape id="Task_1_di" bpmnElement="Task_1">
      <dc:Bounds x="330" y="140" width="100" height="80" />
    </bpmndi:BPMNShape>

    <!-- Gateway (x=505, spacing 175px from Task 1) -->
    <bpmndi:BPMNShape id="Gateway_1_di" bpmnElement="Gateway_1">
      <dc:Bounds x="505" y="155" width="50" height="50" />
      <bpmndi:BPMNLabel>
        <dc:Bounds x="488" y="125" width="84" height="27" />
      </bpmndi:BPMNLabel>
    </bpmndi:BPMNShape>

    <!-- Task 2 (x=630, spacing 125px from Gateway) -->
    <bpmndi:BPMNShape id="Task_2_di" bpmnElement="Task_2">
      <dc:Bounds x="630" y="140" width="100" height="80" />
    </bpmndi:BPMNShape>

    <!-- Task 3 (x=805, spacing 175px from Task 2) -->
    <bpmndi:BPMNShape id="Task_3_di" bpmnElement="Task_3">
      <dc:Bounds x="805" y="140" width="100" height="80" />
    </bpmndi:BPMNShape>

    <!-- Boundary Event on Task 3 (bottom edge) -->
    <bpmndi:BPMNShape id="Event_Boundary_Timer_di" bpmnElement="Event_Boundary_Timer">
      <dc:Bounds x="837" y="202" width="36" height="36" />
    </bpmndi:BPMNShape>

    <!-- End Event (x=980, spacing 175px from Task 3) -->
    <bpmndi:BPMNShape id="Event_End_di" bpmnElement="Event_End">
      <dc:Bounds x="980" y="162" width="36" height="36" />
      <bpmndi:BPMNLabel>
        <dc:Bounds x="970" y="205" width="56" height="14" />
      </bpmndi:BPMNLabel>
    </bpmndi:BPMNShape>

    <!-- Sequence Flows -->
    <bpmndi:BPMNEdge id="Flow_Start_to_Task1_di" bpmnElement="Flow_Start_to_Task1">
      <di:waypoint x="266" y="180" />
      <di:waypoint x="330" y="180" />
    </bpmndi:BPMNEdge>

    <bpmndi:BPMNEdge id="Flow_Task1_to_Gateway_di" bpmnElement="Flow_Task1_to_Gateway">
      <di:waypoint x="430" y="180" />
      <di:waypoint x="505" y="180" />
    </bpmndi:BPMNEdge>

    <bpmndi:BPMNEdge id="Flow_Gateway_to_Task2_di" bpmnElement="Flow_Gateway_to_Task2">
      <di:waypoint x="555" y="180" />
      <di:waypoint x="630" y="180" />
      <bpmndi:BPMNLabel>
        <dc:Bounds x="580" y="162" width="24" height="14" />
      </bpmndi:BPMNLabel>
    </bpmndi:BPMNEdge>

    <bpmndi:BPMNEdge id="Flow_Task2_to_Task3_di" bpmnElement="Flow_Task2_to_Task3">
      <di:waypoint x="730" y="180" />
      <di:waypoint x="805" y="180" />
    </bpmndi:BPMNEdge>

    <bpmndi:BPMNEdge id="Flow_Task3_to_End_di" bpmnElement="Flow_Task3_to_End">
      <di:waypoint x="905" y="180" />
      <di:waypoint x="980" y="180" />
    </bpmndi:BPMNEdge>

    <bpmndi:BPMNEdge id="Flow_Boundary_to_End_di" bpmnElement="Flow_Boundary_to_End">
      <di:waypoint x="855" y="238" />
      <di:waypoint x="855" y="260" />
      <di:waypoint x="998" y="260" />
      <di:waypoint x="998" y="198" />
    </bpmndi:BPMNEdge>

  </bpmndi:BPMNPlane>
</bpmndi:BPMNDiagram>
```

---

**Document Status**: APPROVED
**Tool Compatibility**: Camunda Modeler 5.20.0+
**Testing**: Verified with 11-process hospital revenue cycle
