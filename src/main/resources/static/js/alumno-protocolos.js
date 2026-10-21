
document.addEventListener("DOMContentLoaded", function () {
    if (typeof lucide !== 'undefined') lucide.createIcons();
});

function toggleAcordeon(idItem) {
    const item = document.getElementById(idItem);
    const estaAbierto = item.classList.contains('abierto');

    // Cerrar todos los acordeones
    document.querySelectorAll('.acordeon-item').forEach(acc => {
        acc.classList.remove('abierto');
        acc.querySelector('.flecha-indicador').innerText = '▼';
    });

    // Si no estaba abierto, lo abrimos
    if (!estaAbierto) {
        item.classList.add('abierto');
        item.querySelector('.flecha-indicador').innerText = '▲';
    }

    function descargarTerminos() {
        const enlace = document.createElement('a');
        enlace.href = /*[[@{/docs/Terminos_y_Condiciones_UTP.pdf}]]*/ '/docs/Terminos_y_Condiciones_UTP.pdf';
        enlace.download = 'Terminos_y_Condiciones_UTP.pdf';
        enlace.target = '_blank';
        document.body.appendChild(enlace);
        enlace.click();
        document.body.removeChild(enlace);
    }
}
