// Change profile pic

$(document).ready(function(){
    var readURL = function(input) {
        if (input.files && input.files[0]) {
            var reader = new FileReader();

            reader.onload = function (e) {
                $('.profile-pic').attr('src', e.target.result);
            }
    
            reader.readAsDataURL(input.files[0]);
        }
    }
    

    $(".file-upload").on('change', function(){
        readURL(this);
    });

    // ======= for lang change =======
    $('#lang_toggle').click(function(){
        $('body').toggleClass('ar')
    });


    $('.dash_menu').click(function(e){
        $('body').addClass('dash_menu_show');
        $('.dash_nav_backdrop').css('display', 'block')
    });

    // $(document).click(function(){
    //     $('body').removeClass('dash_menu_show');
    // });
    $('.dash_nav_backdrop').click(function(){
        $('body').removeClass('dash_menu_show');
        $('.dash_nav_backdrop').css('display', 'none')
    })
})


$('.card-box .icon').click(function(){
    $(this).toggleClass('active');
    $('.hku_alert, .alert_backdrop').css("display", "block")
});
// ======= to show alert when remove item from favourite =======
$('.undo_button, .close_hku_alert').click(function(){
    $('.hku_alert, .alert_backdrop').css("display", "none");
    $('.card-box').removeClass('active');
});

$(document).on('click', '.favorite .icon', function(event){
    event.preventDefault()
})




