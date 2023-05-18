const path = require('path')
const fs = require('fs')
const formidable = require("formidable");

module.exports = {
    saveFile: async(request, response) => {
        const form = formidable.IncomingForm()
        return new Promise((resolve)=>{
            form.parse(request, async(err, fields, files) => {
                if(err) return null
                const uploadFolder = path.join(__dirname,"../../uploads",fields.album);
                if (!fs.existsSync(uploadFolder)){
                    fs.mkdirSync(uploadFolder, { recursive: true });
                }
                form.uploadDir = uploadFolder
                const file = files.file
                const new_path = path.join(uploadFolder, "upload_" + Date.now().toString() + ".jpg")
                try {
                    fs.renameSync(file.path, new_path);
                    resolve({
                        album: fields.album,
                        originalName: file.name,
                        url: new_path
                    })
                  } catch (error) {
                    response.writeHead(404, {'Content-Type': 'text/html'})
                    response.write(error)
                    response.end()
                }    
            })
        })
    },
    deleteFile: (url) => {
        fs.unlinkSync(url)
    }  
}