class File
        constructor: (data) ->
                #self = @
                { @isDirectory, @isSymLink, @isFile, @replication, @path, @owner, @group, @permission, @modification_time } = data
                @selected = ko.observable(false)
                #@selectItem = () -> console.log "ping"; self.selected(!self.selected()); return true;
                @date = new Date(@modification_time)
                [_, _...,@name] = @path.split "/"
                if @isDirectory
                     @fileTypeClass = "fa-folder"
                else if @isSymlink
                     @fileTypeClass = "fa-external-link"
                else
                     @fileTypeClass = "fa-file"

        
class DfsView
        constructor: () ->
                self = @
                [_,p] = /\/dfs\/browse\/(.*)/.exec(window.location.href)
                @path = ko.observable(p)
                folders = (f for f in @path().split "/" when f)
                @pathComponents = ({link: "/" + (folders[0..folders.indexOf(f)]).join("/"), folderName: f} for f in folders)
                @files = ko.observableArray([])
                @isRoot = (@path() == "/")
                @getData = () ->
                        $.getJSON "/dfs/listdir/" + @path(), (data) ->
                                self.files(new File(f) for f in data)
                @getData()
                @selectedItems = () ->
                        (ko.utils.arrayFilter @files(), (item) -> item.selected() )
                @selectedFiles = () ->
                        (ko.utils.arrayFilter @selectedItems(), (item) -> item.isFile)
                @concatEnabled = () ->
                        (@selectedFiles().length > 1) && (@selectedFiles().length == @selectedItems().length)
                @concatStr = () -> (@selectedItems().map (item) -> item.path).join("::")
                @downloadFiles = () ->
                        window.location.href = "/dfs/download/" + if @selectedItems().length == @files.length then @path() else @concatStr()
                @concatFiles = () ->
                        if @concatEnabled()
                                window.location.href = "/dfs/concat/" + @concatStr()
                @allSelected = ko.observable(false)
                @selectAll = () ->
                        ko.utils.arrayForEach @files(), (item) ->
                                item.selected(!self.allSelected())
                        self.allSelected(!self.allSelected())
                        return true
                @updatePath = () ->
                        console.log $(@)
                @editPath = () ->
                        $("#pathInput").show()
                        
                

$ ->
    ko.applyBindings(new DfsView)
