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
                @newFileName = ko.observable()
                @newDirName = ko.observable()
                @createFile = () ->
                        $.post "/dfs/file/new", {path: @path(), name: @newFileName()}, (success) ->
                                self.getData()
                                self.newFileName(null)
                                $("#newFile").modal('hide')
                @createDir = () ->
                        $.post "/dfs/dir/new", {path: @path(), name: @newDirName()}, (success) ->
                                self.getData()
                                self.newDirName(null)
                                $("#newDir").modal('hide')
                @chownUsername = ko.observable()
                @chownGroup = ko.observable()
                @chown = () ->
                        if !@chownUsername && !@chownGroup
                                $("#chownErrors").show().html("Either username or group should be specified")
                                return false
                @selectedFileName = () ->
                        if (@selectedItems().length != 1)
                                null
                        else
                                @selectedItems()[0].name
                @rename = () ->
                        $.post "/dfs/file/rename", {path: @selectedItems()[0].path, name: $("#renameDstName").val()}, () ->
                                self.getData()
                                $("#rename").modal('hide')
                        

$ ->
    ko.applyBindings(new DfsView)

