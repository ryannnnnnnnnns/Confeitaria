const toggleMenuButton = document.getElementById('toggleMenu');
if (toggleMenuButton) {
    toggleMenuButton.addEventListener('click', function () {
        const menu = document.getElementById('menuLateral');
        menu.classList.toggle('recolhido');
    });
}

function showAlert(type, message, isHtml = false) {
    if (!message || (Array.isArray(message) && message.length === 0)) {
        return;
    }

    let config = {
        confirmButtonColor: '#ff7700'
    };

    if (isHtml) {
        config.html = message;
    } else {
        config.text = message;
    }

    if (type === 'success') {
        config.icon = 'success';
        config.title = 'Sucesso!';
        config.confirmButtonColor = '#2ecc71';
    } else if (type === 'error') {
        config.icon = 'error';
        config.title = 'Oops...';
        config.confirmButtonColor = '#e74c3c';
    }

    Swal.fire(config);
}