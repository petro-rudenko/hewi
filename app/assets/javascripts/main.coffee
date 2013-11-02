#TODO: Make notifications!
notify = (title, message, type) ->
        alert_div = '<div class="alert alert-#{type} fade in">
                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                <h4>#{title}</h4>
                #{message}
                </div>'
        $(alert_div).alert()
        
@success = (title, message="") ->
        notify(title, message, 'success')
        
@info = (title, message="") ->
        notify(title, message, 'info')

@error = (title, message="") ->
        notify(title, message, 'error')

@warning = (title, message="") ->
        notify(title, message, 'warning')

                
$(document).ready ->
	$("a").tooltip()
