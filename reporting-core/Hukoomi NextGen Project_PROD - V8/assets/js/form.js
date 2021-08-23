//polyfill for ie browser
window.MSInputMethodContext && document.documentMode && document.write('<script src="https://cdn.jsdelivr.net/gh/nuxodin/ie11CustomProperties@4.1.0/ie11CustomProperties.min.js"><\x2fscript>');

$(function () {
    $('select').selectpicker();
});
$(document).ready(function(){
  

    // ======= timepcker =======
    var disabledDate = ['10/10/2020', '10/11/2020','10/12/2020'];
  
    $("input[id^='dateTimeInput_']").datetimepicker({
        format: 'DD/MM/YYYY hh:mm a',
        // debug: true,
        // locale: 'ar',  //language
        // disabledDates: disabledDate,  //disable dates
        minDate: '10/05/2020',  //previous dates from this will be disabled
        maxDate: '10/20/2020',  //future dates from this will be disabled
        icons: {
            time: 'fa fa-chevron-clock',
            date: 'fa fa-calendar-alt',
            up: 'fa fa-chevron-up',
            down: 'fa fa-chevron-down',
            previous: 'fa fa-chevron-left',
            next: 'fa fa-chevron-right',
        },
        widgetPositioning: {  //use only with arabic view
            // horizontal: 'right'
        }
        
    });
// ======= datepicker =======

$("input[id^='dateInput_']").datetimepicker({
    format: 'DD/MM/YYYY',
    disabledDates: disabledDate,
    icons: {
        time: 'fa fa-chevron-clock',
            date: 'fa fa-calendar-alt',
            up: 'fa fa-chevron-up',
            down: 'fa fa-chevron-down',
            previous: 'fa fa-chevron-left',
            next: 'fa fa-chevron-right',
    }
});



// ======= validation =======
    // JavaScript for disabling form submissions if there are invalid fields
    (function () {
        'use strict';
        window.addEventListener('load', function () {
            // Fetch all the forms we want to apply custom Bootstrap validation styles to
            var forms = document.getElementsByClassName('needs-validation');
            // Loop over them and prevent submission
            var validation = Array.prototype.filter.call(forms, function (form) {
                form.addEventListener('submit', function (event) {
                    if (form.checkValidity() === false) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                    form.classList.add('was-validated');
                }, false);
            });
        }, false);
    })();
})