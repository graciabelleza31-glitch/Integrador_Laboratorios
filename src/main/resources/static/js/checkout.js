
lucide.createIcons();

function mostrarModalDevolucion(boton) {
    const id = boton.getAttribute('data-id');
    const lab = boton.getAttribute('data-lab');
    const resp = boton.getAttribute('data-resp');
    const horario = boton.getAttribute('data-horario');
    const items = boton.getAttribute('data-items');

    document.getElementById('campoIdReservaOculto').value = id;
    document.getElementById('textoModalSubtitulo').innerText = lab + ' · ' + id;
    document.getElementById('textoModalResponsable').innerText = resp;
    document.getElementById('textoModalHorario').innerText = horario;
    document.getElementById('textoModalItems').innerText = items;

    document.getElementById('modalDevolucion').style.display = 'flex';
    lucide.createIcons();
}

function cerrarModalDevolucion() {
    document.getElementById('modalDevolucion').style.display = 'none';
}
