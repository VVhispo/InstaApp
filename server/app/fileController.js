const {Photo} = require("./model")
const path = require('path')
const fs = require('fs')
module.exports = {
    saveFile: (form, request) => {
        form.parse(request, async(err, fields, files) => {
            if(err) return null
            const uploadFolder = path.join(__dirname,"../uploads",fields.album);
            if (!fs.existsSync(uploadFolder)){
                fs.mkdirSync(uploadFolder, { recursive: true });
            }
            form.uploadDir = uploadFolder
            const file = files.file
            const new_path = path.join(uploadFolder, "upload_" + Date.now().toString() + ".jpg")
            try {
                fs.renameSync(file.path, new_path);
              } catch (error) {
                console.log(error);
            }
            const new_photo = new Photo(fields.album, file.name, new_path)
           })
    },  
}