# Landing de GasApp

Carpeta con la web pública de GasApp. **Hosting destino: IONOS** (también
funciona en cualquier hosting estático: HTML + CSS planos, sin build).

## Archivos

```
docs/
├── index.html        Landing principal
├── privacy.html      Política de privacidad
├── styles.css        Estilos
├── icon.png          Logo (512×512)
└── .htaccess         Gzip, caché y cabeceras (Apache / IONOS)
```

## Estructura de dominios

- **`gasapp.cloud`** → redirección 301 hacia **`landing.gasapp.cloud`**.
- **`landing.gasapp.cloud`** → sirve esta landing (los archivos de `docs/`).

En IONOS:
1. *Dominios y SSL* → crea el subdominio **`landing.gasapp.cloud`** y
   asígnale una carpeta del webspace (p. ej. `/landing/`).
2. En *Dominio `gasapp.cloud`* → *Redirección* → configura **301** hacia
   `https://landing.gasapp.cloud/`.

## Subida a IONOS

### Opción A — Panel de IONOS (sin programas)
1. **Panel de IONOS** → *Hosting* → *Webspace Explorer* / *File Manager*.
2. Entra en la carpeta del subdominio `landing.gasapp.cloud` (la que
   asignaste arriba, p. ej. `/landing/`).
3. **Sube los 5 archivos** (`index.html`, `privacy.html`, `styles.css`,
   `icon.png`, `.htaccess`). Si el explorador oculta archivos que empiezan
   por punto, márcalos visibles para subir `.htaccess`.
4. Abre `https://landing.gasapp.cloud` → listo.

### Opción B — FTP/SFTP con FileZilla u otro cliente

- **Servidor / usuario / contraseña:** los de *Datos de acceso FTP* de IONOS.
- **Carpeta destino:** la del subdominio `landing.gasapp.cloud`.
- Arrastra los archivos.

## URL de la política de privacidad

Esta es la URL que va en la ficha de **Google Play Console** (campo
*Política de privacidad*):

```
https://landing.gasapp.cloud/privacy.html
```

## Cambios futuros

El `.htaccess` cachea imágenes/CSS 30 días → si actualizas el icono o estilos
y no ves los cambios, fuerza una recarga (Ctrl+F5) o renombra el archivo
(p. ej. `icon-v2.png`).

## ZIP listo para subir

Como atajo, hay un script que empaqueta todo. Genera `landing-ionos.zip`
en la raíz del repo:

```
./scripts/package-landing.sh
```

(O lo descomprimes y subes a IONOS.)
