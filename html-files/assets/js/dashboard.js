// ======= validation =======
// Example starter JavaScript for disabling form submissions if there are invalid fields
(function () {
    'use strict';
    window.addEventListener('load', function () {
        // Fetch all the forms we want to apply custom Bootstrap validation styles to
        var forms = document.getElementsByClassName('needs-validation');
        // Loop over them and prevent submission
        var validation = Array.prototype.filter.call(forms, function (form) {
            form.addEventListener('submit', function (event) {
                if (!form.checkValidity() === false) {
                    $('.hku_alert, .alert_backdrop').css("display", "block");
                    event.preventDefault();
                    event.stopPropagation();
                    setTimeout(function () {
                        $('.hku_alert, .alert_backdrop').css("display", "none")
                    }, 5000)
                }
                else {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        });
    }, false);
})();

// ======= show more =======
function show_more_less(e) {
    console.log(e)
    var current = e;
    if (!$(current).hasClass('show_less')) {
        $(current).find('span').text('Show Less');
        $(current).addClass('show_less')
        $(current).parents('.bills_card_wrapper').find('.hku_card').addClass('show_all')
    }
    else {
        $(current).find('span').text('Show All');
        $(current).removeClass('show_less')
        $(current).parents('.bills_card_wrapper').find('.hku_card').removeClass('show_all')
    }
}

// ======= to show alert when remove item from favourite =======
// $('.subscribe_btn').click(function(){
//         console.log("favourite")
//         $('.hku_alert, .alert_backdrop').css("display", "block")
//     });

$('.undo_button, .close_hku_alert').click(function () {
    console.log("favourite")
    $('.hku_alert, .alert_backdrop').css("display", "none")
});
// ========= to show menu on small screens =======
// $('.dash_menu').click(function(){
//     $('body').toggleClass('dash_menu_show')
// })
$('.dash_menu').click(function (e) {
    // e.stopPropagation();
    $('body').toggleClass('dash_menu_show')
});

$(document).click(function () {
    $('body').removeClass('dash_menu_show');
});

$('#lang_toggle').click(function () {
    $('body').toggleClass('ar')
});

function checkAll(ele) {
    var checkboxes = document.getElementsByTagName('input');
    if ($(checkboxes).hasClass('bill_check')) {
        if (ele.checked) {
            for (var i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i].type == 'checkbox') {
                    checkboxes[i].checked = true;
                }
            }
        } else {
            for (var i = 0; i < checkboxes.length; i++) {
                console.log(i)
                if (checkboxes[i].type == 'checkbox') {
                    checkboxes[i].checked = false;
                }
            }
        }
    }

}

$('.bill_check').on('change', function () {
    if (!$(this).is(':checked')) {
        $('#servicesck').prop('checked', false)
    }
})
$(function () {
    $('[data-toggle="popover"]').popover({
        container: 'body'
    })
});

$('#business').click(function () {
    window.location.href = 'business-profile.html'
});
$('#personal').click(function () {
    window.location.href = 'dashboard-data.html'
});



$('#polls').on('slide.bs.carousel', function () {
    console.log($(carousel-item ))
  })