$('#lang_toggle').click(function(){
    $('body').toggleClass('ar')
});

$(function () {
    $('#datetimepicker').datetimepicker({
        format: 'L',
    });
});



$('.filter-toggle').click(function(e){    
    e.stopPropagation();   
    $('.filter-dropdown').toggleClass('show');
});

$('.clearAll').click(function(e){
    e.stopPropagation(); 
    $('.filter-card').find('.checkbox').prop('checked', false);
});

$('body').click(function(e){
    if(!($(e.target).parents('.filter-notification').find('.filter-dropdown').length)){        
        $('.filter-dropdown').removeClass('show');
    }
});
