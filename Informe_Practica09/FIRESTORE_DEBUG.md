# 🔧 Guía de Solución - Error de Permisos en Firestore

## Error Actual
```
PERMISSION_DENIED: Missing or insufficient permissions
```

## ✅ Paso 1: Verificar Reglas de Firestore

### Opción A: Reglas Temporales para Testing (MUY PERMISIVAS)

**Copia EXACTAMENTE esto en Firebase Console:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

⚠️ **IMPORTANTE:** Estas reglas permiten TODO sin autenticación. Solo para pruebas.

### Instrucciones:
1. Ve a https://console.firebase.google.com/
2. Selecciona tu proyecto
3. Firestore Database → Reglas
4. **BORRA TODO** lo que está ahí
5. Pega EXACTAMENTE el código de arriba
6. Haz clic en "Publicar"
7. **Espera 30 segundos** antes de probar

---

## ✅ Paso 2: Si las reglas temporales funcionan...

Entonces el problema está en las reglas. Usa estas reglas de producción:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Colección de ratings
    match /ratings/{ratingId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

---

## ✅ Paso 3: Verificar que el usuario esté autenticado

El problema podría ser que `request.auth` es null. Verifica en el código.

---

## 🔍 Diagnóstico Adicional

### Revisa el Log de Firebase Console:
1. Ve a Firestore Database → Uso
2. Mira las solicitudes rechazadas
3. Verifica el motivo del rechazo

### Verifica la Base de Datos Correcta:
- ¿Tienes múltiples proyectos de Firebase?
- ¿Estás viendo las reglas del proyecto correcto?
- ¿El archivo google-services.json es del proyecto correcto?
