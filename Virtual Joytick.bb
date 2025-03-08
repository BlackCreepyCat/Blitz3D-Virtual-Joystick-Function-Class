; ----------------------------------------
; Name : Virtual Joystick by Filax
; Date : (C)2025 
; Site : https://github.com/BlackCreepyCat
; ----------------------------------------

; Structure pour le Joystick Virtuel
Type VirtualJoystick
    Field X%          ; Position X absolue du joystick (coin supérieur gauche)
    Field Y%          ; Position Y absolue du joystick (coin supérieur gauche)
    Field Radius%     ; Rayon du joystick (diamètre / 2)
    Field ButtonX#    ; Position X relative du bouton (flottant pour précision)
    Field ButtonY#    ; Position Y relative du bouton (flottant pour précision)
    Field IsPressed%  ; État : est-ce que le joystick est pressé ?
    Field ValueX#     ; Valeur X (-1.0 à 1.0) selon déplacement
    Field ValueY#     ; Valeur Y (-1.0 à 1.0) selon déplacement
End Type

; Fonction pour créer un joystick virtuel
Function CreateVirtualJoystick.VirtualJoystick(X%, Y%, Radius%)
    Local J.VirtualJoystick = New VirtualJoystick
    
    J\X% = X%
    J\Y% = Y%
    J\Radius% = Radius%
    J\ButtonX# = 0.0        ; Centre initial (relatif au centre du joystick)
    J\ButtonY# = 0.0        ; Centre initial (relatif au centre du joystick)
    J\IsPressed% = False
    J\ValueX# = 0.0         ; Valeur initiale
    J\ValueY# = 0.0         ; Valeur initiale
    
    Return J
End Function

; Fonction pour mettre à jour le joystick
Function UpdateVirtualJoystick(J.VirtualJoystick)
    If J = Null Then Return
    
    Local CenterX# = J\X% + J\Radius%  ; Centre absolu X du joystick
    Local CenterY# = J\Y% + J\Radius%  ; Centre absolu Y du joystick
    
    ; Vérifier si la souris est dans la zone du joystick
    Local MouseInZone% = (MouseX() >= J\X% And MouseX() <= J\X% + J\Radius% * 2 And MouseY() >= J\Y% And MouseY() <= J\Y% + J\Radius% * 2)
    
    ; Gestion de l'état pressé
    If MouseInZone% And MouseDown(1) Then
        J\IsPressed% = True
    ElseIf Not MouseDown(1) Then
        J\IsPressed% = False
    EndIf
    
    If J\IsPressed% Then
        ; Calculer la position relative du bouton par rapport au centre
        J\ButtonX# = MouseX() - CenterX#
        J\ButtonY# = MouseY() - CenterY#
        
        ; Calculer la distance depuis le centre
        Local Distance# = Sqr(J\ButtonX# * J\ButtonX# + J\ButtonY# * J\ButtonY#)
        
        ; Limiter le déplacement au rayon
        If Distance# > J\Radius% Then
            Local Angle# = ATan2(J\ButtonY#, J\ButtonX#)
            J\ButtonX# = Cos(Angle#) * J\Radius%
            J\ButtonY# = Sin(Angle#) * J\Radius%
        EndIf
        
        ; Calculer les valeurs normalisées (-1.0 à 1.0)
        J\ValueX# = J\ButtonX# / Float(J\Radius%)
        J\ValueY# = J\ButtonY# / Float(J\Radius%)
    Else
        ; Revenir au centre quand relâché (transition douce optionnelle)
        J\ButtonX# = J\ButtonX# * 0.9
        J\ButtonY# = J\ButtonY# * 0.9
        J\ValueX# = J\ButtonX# / Float(J\Radius%)
        J\ValueY# = J\ButtonY# / Float(J\Radius%)
        If Abs(J\ButtonX#) < 0.1 Then J\ButtonX# = 0.0
        If Abs(J\ButtonY#) < 0.1 Then J\ButtonY# = 0.0
    EndIf
End Function

; Fonction pour dessiner le joystick
Function DrawVirtualJoystick(J.VirtualJoystick)
    If J = Null Then Return
    
    Local CenterX# = J\X% + J\Radius%
    Local CenterY# = J\Y% + J\Radius%
    
    ; Dessiner le fond du joystick (cercle gris)
    Color 100, 100, 100
    Oval J\X%, J\Y%, J\Radius% * 2, J\Radius% * 2, 1
    
    ; Dessiner le bouton (cercle plus petit, couleur dynamique)
    If J\IsPressed% Then
        Color 255, 100, 100  ; Rouge clair quand pressé
    Else
        Color 200, 200, 200  ; Gris clair par défaut
    EndIf
    Oval CenterX# + J\ButtonX# - J\Radius% / 2, CenterY# + J\ButtonY# - J\Radius% / 2, J\Radius%, J\Radius%, 1
    
    ; Dessiner les bordures
    Color 0, 0, 0
    Oval J\X%, J\Y%, J\Radius% * 2, J\Radius% * 2, 0
End Function

; Exemple d'utilisation
Graphics3D 800, 600, 0, 2
SetBuffer BackBuffer()

; Create joystick
Local Joystick.VirtualJoystick = CreateVirtualJoystick(30, 450, 70)

; Create scene
Local Cube = CreateCube()
EntityColor Cube, 100, 100, 100
PositionEntity Cube, 0, 0, 0

Local Cam = CreateCamera()
CameraRange Cam, 0.01, 1000 
PositionEntity Cam, 0, 0, -3

Local Light2 = CreateLight(3)
LightColor Light2, 255, 80, 60
LightConeAngles Light2, 0, 95
PositionEntity Light2, -15, 10, -14.5
LightRange Light2, 18
PointEntity Light2, Cube

Local Light3 = CreateLight(3)
LightColor Light3, 70, 80, 255
LightConeAngles Light3, 0, 95
PositionEntity Light3, 15, 10, -14.5
LightRange Light3, 18
PointEntity Light3, Cube

Repeat
    Cls
    
    ; Mettre à jour le joystick avant le rendu 3D
    UpdateVirtualJoystick(Joystick)
    
    ; Appliquer la rotation au cube
    TurnEntity Cube, Joystick\ValueX# * 2, Joystick\ValueY# * 2, 0
    
    ; Rendu 3D
    RenderWorld
    
    ; Dessiner le joystick et le texte après le rendu 3D
    DrawVirtualJoystick(Joystick)
    
    Color 255, 0, 0
    Text 10, 10, "ValueX: " + Joystick\ValueX#
    Text 10, 30, "ValueY: " + Joystick\ValueY#
    
    Flip
Until KeyHit(1) ; Quitter avec Échap

FreeEntity Cube
FreeEntity Cam
FreeEntity Light2
FreeEntity Light3
Delete Joystick
End
