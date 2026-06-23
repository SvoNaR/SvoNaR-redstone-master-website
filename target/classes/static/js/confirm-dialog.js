(function () {
	const dialog = document.getElementById('confirm-dialog');
	if (!dialog) {
		return;
	}

	const messageEl = dialog.querySelector('.confirm-dialog__message');
	const acceptButton = dialog.querySelector('[data-confirm-accept]');
	let pendingForm = null;

	function closeDialog() {
		dialog.hidden = true;
		dialog.setAttribute('aria-hidden', 'true');
		pendingForm = null;
		document.body.classList.remove('confirm-dialog-open');
	}

	function openDialog(message, form) {
		pendingForm = form;
		messageEl.textContent = message;
		dialog.hidden = false;
		dialog.setAttribute('aria-hidden', 'false');
		document.body.classList.add('confirm-dialog-open');
		acceptButton.focus();
	}

	document.addEventListener('submit', function (event) {
		const form = event.target;
		if (!(form instanceof HTMLFormElement)) {
			return;
		}
		const message = form.dataset.confirmMessage;
		if (!message) {
			return;
		}
		if (form.dataset.confirmApproved === 'true') {
			delete form.dataset.confirmApproved;
			return;
		}
		event.preventDefault();
		openDialog(message, form);
	}, true);

	acceptButton.addEventListener('click', function () {
		if (!pendingForm) {
			return;
		}
		const form = pendingForm;
		closeDialog();
		form.dataset.confirmApproved = 'true';
		form.requestSubmit();
	});

	dialog.querySelectorAll('[data-confirm-cancel]').forEach(function (button) {
		button.addEventListener('click', closeDialog);
	});

	document.addEventListener('keydown', function (event) {
		if (dialog.hidden) {
			return;
		}
		if (event.key === 'Escape') {
			closeDialog();
		}
	});
})();
